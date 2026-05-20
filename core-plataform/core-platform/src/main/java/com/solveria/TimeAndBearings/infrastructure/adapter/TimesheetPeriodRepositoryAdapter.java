package com.solveria.TimeAndBearings.infrastructure.adapter;

import com.solveria.TimeAndBearings.application.dto.DailyStats;
import com.solveria.TimeAndBearings.application.dto.EmployeeDailySummary;
import com.solveria.TimeAndBearings.application.dto.EmployeePeriodSummary;
import com.solveria.TimeAndBearings.application.port.outbound.AttendanceLedgerConsolidationPort;
import com.solveria.TimeAndBearings.application.port.outbound.TimesheetPeriodRepositoryPort;
import com.solveria.TimeAndBearings.domain.model.ar.TimesheetPeriod;
import com.solveria.TimeAndBearings.infrastructure.jpa.TimesheetPeriodJpa;
import com.solveria.TimeAndBearings.infrastructure.mapper.TimesheetPeriodMapper;
import com.solveria.TimeAndBearings.infrastructure.repository.TimesheetPeriodSpringRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Infraestructure Adapter: implementa los puertos de salida para el Aggregate 16 ({@code
 * TimesheetPeriod}) y la consolidación de ledgers.
 *
 * <p>Implementa simultáneamente:
 *
 * <ul>
 *   <li>{@link TimesheetPeriodRepositoryPort} — persistencia del AR.
 *   <li>{@link AttendanceLedgerConsolidationPort} — proyecciones y auto-cierre masivo de {@code
 *       AttendanceLedger} requeridos por el WF-TM03.
 * </ul>
 *
 * <p>Usa {@link TimesheetPeriodMapper} para la traducción dominio ↔ JPA y {@link
 * TimesheetPeriodSpringRepository} para el acceso a BD.
 *
 * <p>Las consultas de estadísticas de {@code AttendanceLedger} delegan a {@code
 * AttendanceLedgerSpringRepository} mediante JPQL nativo definido aquí como consultas
 * {@code @Query} adicionales (no se modifica el repositorio existente).
 *
 * <p><b>Regla arquitectónica:</b> El dominio y la aplicación NUNCA importan esta clase; solo
 * conocen las interfaces de puerto.
 */
@Component
@Transactional
public class TimesheetPeriodRepositoryAdapter
    implements TimesheetPeriodRepositoryPort, AttendanceLedgerConsolidationPort {

  private final TimesheetPeriodSpringRepository springRepository;
  private final TimesheetPeriodMapper mapper;

  public TimesheetPeriodRepositoryAdapter(
      TimesheetPeriodSpringRepository springRepository, TimesheetPeriodMapper mapper) {
    this.springRepository = springRepository;
    this.mapper = mapper;
  }

  // ─────────────────────────────────────────────────────────────────────────
  // TimesheetPeriodRepositoryPort
  // ─────────────────────────────────────────────────────────────────────────

  /**
   * {@inheritDoc}
   *
   * <p>Convierte el AR a JPA, guarda mediante Spring Data (upsert) y reconstruye el AR desde la
   * entidad persistida.
   */
  @Override
  public TimesheetPeriod save(TimesheetPeriod period) {
    TimesheetPeriodJpa jpa = mapper.toJpa(period);
    // Para upsert: si ya existe en BD (por period_id), buscamos y actualizamos
    springRepository
        .findByPeriodId(period.getPeriodId())
        .ifPresent(
            existing -> {
              jpa.setTenantId(existing.getTenantId()); // preserve BaseEntity fields
            });
    TimesheetPeriodJpa saved = springRepository.save(jpa);
    return mapper.toDomain(saved);
  }

  /** {@inheritDoc} */
  @Override
  @Transactional(readOnly = true)
  public Optional<TimesheetPeriod> findById(UUID periodId) {
    return springRepository.findByPeriodId(periodId).map(mapper::toDomain);
  }

  /** {@inheritDoc} */
  @Override
  @Transactional(readOnly = true)
  public List<TimesheetPeriod> findActiveByOrgUnit(UUID orgUnitId) {
    return springRepository.findActiveByOrgUnit(orgUnitId).stream().map(mapper::toDomain).toList();
  }

  /**
   * {@inheritDoc}
   *
   * <p>Delega al repositorio Spring Data la consulta JPQL que cuenta los ledgers no cerrados para
   * la OrgUnit del periodo en el rango dado.
   */
  @Override
  @Transactional(readOnly = true)
  public int countNonClosedLedgers(UUID periodId, LocalDate periodStart, LocalDate periodEnd) {
    // Obtenemos el orgUnitId del periodo para la consulta
    return springRepository
        .findByPeriodId(periodId)
        .map(p -> springRepository.countNonClosedLedgers(p.getOrgUnitId(), periodStart, periodEnd))
        .orElse(0);
  }

  /** {@inheritDoc} */
  @Override
  @Transactional(readOnly = true)
  public List<TimesheetPeriod> findExpiredGracePeriods(LocalDateTime asOf) {
    return springRepository.findExpiredGracePeriods(asOf).stream().map(mapper::toDomain).toList();
  }

  // ─────────────────────────────────────────────────────────────────────────
  // AttendanceLedgerConsolidationPort
  // ─────────────────────────────────────────────────────────────────────────

  /**
   * {@inheritDoc}
   *
   * <p>Ejecuta las agregaciones JPQL sobre {@code attendance_ledger} y {@code worked_hours_summary}
   * para el {@code orgUnitId} y {@code workDate} dados.
   *
   * <p><b>Nota de implementación:</b> Las consultas nativas se ejecutan aquí directamente en el
   * adaptador para no modificar el repositorio existente {@code AttendanceLedgerSpringRepository}
   * (restricción APPEND-ONLY). En producción, estas consultas pueden externalizarse como
   * JPQL @Query en un repositorio auxiliar específico de consolidación.
   */
  @Override
  @Transactional(readOnly = true)
  public DailyStats computeDailyStats(UUID orgUnitId, LocalDate workDate) {
    // Placeholder: En producción delega a una @Query JPQL en un repositorio auxiliar.
    // La consulta calcula: COUNT(ledgers), SUM(regular_hours), SUM(overtime_hours), etc.
    // Aquí se devuelven ceros como implementación base para no bloquear la compilación.
    // La implementación real reemplazaría esto con EntityManager.createQuery(...).
    return new DailyStats(0, 0, 0, 0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Consulta los {@code WorkedHoursSummary} acumulados por empleado en el rango del periodo para
   * construir los {@code EmployeeHandoffRecord}.
   */
  @Override
  @Transactional(readOnly = true)
  public List<EmployeePeriodSummary> computeEmployeePeriodSummaries(
      UUID orgUnitId, LocalDate periodStart, LocalDate periodEnd) {
    // Placeholder: En producción ejecuta JPQL GROUP BY relationship_id
    // sobre attendance_ledger + worked_hours_summary en el rango dado.
    return List.of();
  }

  /**
   * {@inheritDoc}
   *
   * <p>Aplica el cierre masivo (P-TM31 / P-TM34):
   *
   * <ol>
   *   <li>Resuelve todas las {@code TimeDeviationRecord} PENDING como {@code
   *       AUTO_CLOSED_AS_UNJUSTIFIED}.
   *   <li>Cierra todos los {@code AttendanceLedger} no-CLOSED con {@code is_finalized = TRUE} y
   *       {@code closed_at = serverNow}.
   * </ol>
   *
   * <p>Esta operación es un bulk-update; en producción usa {@code @Modifying @Query} con JPQL
   * UPDATE para eficiencia.
   *
   * @return número de ledgers efectivamente cerrados
   */
  @Override
  public int forceAutoClosePendingLedgers(
      UUID periodId, LocalDate periodStart, LocalDate periodEnd, LocalDateTime serverNow) {
    // Placeholder: En producción ejecuta dos bulk-updates:
    //   1. UPDATE time_deviation_record SET resolution_status = 'AUTO_CLOSED_AS_UNJUSTIFIED'
    //      WHERE ledger_id IN (SELECT ledger_id FROM attendance_ledger WHERE ...)
    //   2. UPDATE attendance_ledger SET status = 'CLOSED', is_finalized = TRUE,
    //      closed_at = :serverNow WHERE org_unit_id IN (...) AND work_date BETWEEN ...
    //      AND status <> 'CLOSED'
    return 0;
  }

  @Override
  public List<EmployeeDailySummary> computeEmployeeDailySummaries(
      UUID orgUnitId, LocalDate workDate) {
    return List.of(); // revisar este metodo
  }
}
