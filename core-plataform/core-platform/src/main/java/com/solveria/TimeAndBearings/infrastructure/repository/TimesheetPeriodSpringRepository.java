package com.solveria.TimeAndBearings.infrastructure.repository;

import com.solveria.TimeAndBearings.infrastructure.jpa.TimesheetPeriodJpa;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data JPA Repository para {@link TimesheetPeriodJpa} (Aggregate 16).
 *
 * <p>Usada exclusivamente por {@code TimesheetPeriodRepositoryAdapter}. No debe ser inyectada
 * directamente en capas de aplicación o dominio.
 */
public interface TimesheetPeriodSpringRepository extends JpaRepository<TimesheetPeriodJpa, Long> {

  /**
   * Busca un {@code TimesheetPeriod} por su UUID de negocio ({@code period_id}).
   *
   * @param periodId UUID de negocio del periodo
   * @return el registro si existe
   */
  Optional<TimesheetPeriodJpa> findByPeriodId(UUID periodId);

  /**
   * Recupera todos los periodos de una OrgUnit en estado {@code OPEN} o {@code IN_GRACE_PERIOD},
   * para el CRON de consolidación.
   *
   * @param orgUnitId FK a OrgUnit (BC-01)
   * @return lista de periodos activos
   */
  @Query(
      """
            SELECT p FROM TimesheetPeriodJpa p
            WHERE p.orgUnitId = :orgUnitId
              AND p.status IN ('OPEN', 'IN_GRACE_PERIOD')
            ORDER BY p.periodStart ASC
            """)
  List<TimesheetPeriodJpa> findActiveByOrgUnit(@Param("orgUnitId") UUID orgUnitId);

  /**
   * Recupera todos los periodos cuyo {@code grace_period_end} venció y que aún están en status
   * {@code OPEN} o {@code IN_GRACE_PERIOD}.
   *
   * <p>Utilizado por el CRON de evaluación del Periodo de Gracia (P-TM34) a las 17:05 para disparar
   * el auto-cierre masivo.
   *
   * @param asOf timestamp de referencia (servidor NTP)
   * @return lista de periodos elegibles para auto-cierre
   */
  @Query(
      """
            SELECT p FROM TimesheetPeriodJpa p
            WHERE p.gracePeriodEnd < :asOf
              AND p.status IN ('OPEN', 'IN_GRACE_PERIOD')
            ORDER BY p.gracePeriodEnd ASC
            """)
  List<TimesheetPeriodJpa> findExpiredGracePeriods(@Param("asOf") LocalDateTime asOf);

  /**
   * Cuenta el número de {@code AttendanceLedger} en el rango del periodo que NO tienen status
   * {@code CLOSED}.
   *
   * <p>Usado para validar la invariante P-TM33 antes del cierre. La consulta une {@code
   * attendance_ledger} con el rango de fechas del periodo.
   *
   * @param orgUnitId OrgUnit del periodo
   * @param periodStart primer día del rango
   * @param periodEnd último día del rango
   * @return número de ledgers no cerrados revisar despues los commands si enviar orgunitId para
   *     attendance legder
   */
  @Query(
      """
            SELECT COUNT(al) FROM AttendanceLedgerJpa al
            WHERE al.orgUnitId = :orgUnitId
              AND al.workDate BETWEEN :periodStart AND :periodEnd
              AND al.status <> 'CLOSED'
            """)
  int countNonClosedLedgers(
      @Param("orgUnitId") UUID orgUnitId,
      @Param("periodStart") java.time.LocalDate periodStart,
      @Param("periodEnd") java.time.LocalDate periodEnd);
}
