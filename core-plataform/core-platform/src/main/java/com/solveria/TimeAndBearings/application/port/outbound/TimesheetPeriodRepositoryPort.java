package com.solveria.TimeAndBearings.application.port.outbound;

import com.solveria.TimeAndBearings.domain.model.ar.TimesheetPeriod;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound Port: Repositorio de {@code TimesheetPeriod} (Aggregate 16).
 *
 * <p>Define el contrato que la capa de infraestructura debe cumplir para persistir y recuperar el
 * Aggregate Root {@code TimesheetPeriod} y sus entidades hijas.
 *
 * <p>Regla de arquitectura hexagonal: la capa de dominio y aplicación solo conoce esta interfaz;
 * nunca importa clases de JPA o Spring Data.
 */
public interface TimesheetPeriodRepositoryPort {

  /**
   * Persiste o actualiza un {@code TimesheetPeriod} y sus entidades hijas
   * (DailyConsolidationSummary, PayrollHandoffPackage) en una única transacción.
   *
   * @param period agregado a persistir
   * @return el agregado persistido (con IDs asignados si es nuevo)
   */
  TimesheetPeriod save(TimesheetPeriod period);

  /**
   * Recupera un {@code TimesheetPeriod} por su identificador primario.
   *
   * @param periodId PK del periodo
   * @return el agregado si existe, vacío en caso contrario
   */
  Optional<TimesheetPeriod> findById(UUID periodId);

  /**
   * Recupera todos los {@code TimesheetPeriod} de una OrgUnit que tengan status {@code OPEN} o
   * {@code IN_GRACE_PERIOD}, para el procesamiento del CRON nocturno.
   *
   * @param orgUnitId FK a OrgUnit (BC-01)
   * @return lista (puede ser vacía) de periodos activos
   */
  List<TimesheetPeriod> findActiveByOrgUnit(UUID orgUnitId);

  /**
   * Cuenta el número de {@code AttendanceLedger} dentro del rango del periodo que NO tienen status
   * {@code CLOSED}.
   *
   * <p>Este conteo se usa para validar la invariante P-TM33 antes del cierre.
   *
   * @param periodId FK al periodo
   * @param periodStart primer día del rango
   * @param periodEnd último día del rango
   * @return número de ledgers no cerrados en el rango
   */
  int countNonClosedLedgers(UUID periodId, LocalDate periodStart, LocalDate periodEnd);

  /**
   * Recupera todos los periodos cuyo {@code grace_period_end} venció y que están aún en status
   * {@code IN_GRACE_PERIOD} o {@code OPEN}.
   *
   * <p>Utilizado por el CRON de evaluación del Periodo de Gracia (P-TM34) para disparar el
   * auto-cierre masivo.
   *
   * @param asOf timestamp de referencia (servidor NTP)
   * @return lista de periodos elegibles para auto-cierre
   */
  List<TimesheetPeriod> findExpiredGracePeriods(java.time.LocalDateTime asOf);
}
