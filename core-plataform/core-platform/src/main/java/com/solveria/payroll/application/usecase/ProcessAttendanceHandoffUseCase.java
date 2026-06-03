package com.solveria.payroll.application.usecase;

import com.solveria.TimeAndBearings.application.port.outbound.TimesheetPeriodRepositoryPort;
import com.solveria.TimeAndBearings.domain.event.AttendancePeriodClosedEvent;
import com.solveria.TimeAndBearings.domain.model.ar.TimesheetPeriod;
import com.solveria.TimeAndBearings.domain.model.entity.PayrollHandoffPackage;
import com.solveria.TimeAndBearings.domain.model.vo.EmployeeHandoffRecord;
import com.solveria.payroll.application.port.inbound.AttendanceHandoffUseCase;
import com.solveria.payroll.application.port.outbound.DeductionRecordRepositoryPort;
import com.solveria.payroll.application.port.outbound.IncomeRecordRepositoryPort;
import com.solveria.payroll.application.port.outbound.PayrollPeriodRepositoryPort;
import com.solveria.payroll.domain.model.ar.DeductionRecord;
import com.solveria.payroll.domain.model.ar.IncomeRecord;
import com.solveria.payroll.domain.model.entity.PayrollPeriod;
import com.solveria.payroll.domain.model.vo.DeductionType;
import com.solveria.payroll.domain.model.vo.IncomeType;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case: Procesa el handoff de asistencia desde TimeAndBearings.
 *
 * <p>Implementa {@link AttendanceHandoffUseCase}. Recibe el evento {@code
 * AttendancePeriodClosedEvent} emitido por BC-TM y, por cada empleado en el payload, genera los
 * {@link IncomeRecord} (por horas extra y recargos) y {@link DeductionRecord} (por
 * atrasos/ausencias) correspondientes.
 *
 * <p><b>Workflow Paso 2:</b> "El sistema recibe el resumen validado de horas por empleado: horas
 * regulares, horas extra diurnas, horas nocturnas (recargo 25% LGT Bolivia), horas en feriados
 * (recargo 100%), horas en domingos Retail (recargo 100%)."
 *
 * <p><b>Regla crítica:</b> Payroll no usa datos de Scheduling directamente. La fuente de verdad de
 * horas trabajadas es siempre TM. Si TM no ha cerrado el período, Payroll no puede generar ninguna
 * planilla del mismo período.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessAttendanceHandoffUseCase implements AttendanceHandoffUseCase {

  private final IncomeRecordRepositoryPort incomeRecordRepository;
  private final DeductionRecordRepositoryPort deductionRecordRepository;
  private final TimesheetPeriodRepositoryPort periodRepository;
  private final PayrollPeriodRepositoryPort payrollPeriodRepository;

  @Override
  @Transactional
  public void manualSync(UUID periodId, UUID tenantId) {
    Objects.requireNonNull(periodId, "periodId no puede ser nulo");
    Objects.requireNonNull(tenantId, "tenantId no puede ser nulo");

    log.info(
        "event=PRL_ATTENDANCE_HANDOFF_MANUAL_START periodId={} tenantId={}", periodId, tenantId);

    TimesheetPeriod period =
        periodRepository
            .findById(periodId)
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "No se encontró el período de asistencia: " + periodId));

    if (!period.getTenantId().equals(tenantId)) {
      throw new IllegalArgumentException(
          "El tenantId del período no coincide con el tenantId solicitado");
    }

    // Resolver el PayrollPeriod de nómina (prl_payroll_period) que corresponde
    // al mes/año del TimesheetPeriod. Sus UUIDs son distintos y la FK de
    // prl_deduction_record / prl_income_record apunta a prl_payroll_period.
    int month = period.getPeriodBoundary().periodStart().getMonthValue();
    int year = period.getPeriodBoundary().periodStart().getYear();
    PayrollPeriod payrollPeriod =
        payrollPeriodRepository
            .findByMonthAndYear(month, year, tenantId)
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "No existe un PayrollPeriod para %d/%d y tenantId=%s"
                            .formatted(month, year, tenantId)));
    UUID payrollPeriodId = payrollPeriod.getPeriodId();

    PayrollHandoffPackage handoff = period.getHandoffPackage();
    if (handoff == null || handoff.getEmployeeRecords() == null) {
      log.warn(
          "event=PRL_ATTENDANCE_HANDOFF_MANUAL_NO_DATA periodId={} tenantId={}",
          periodId,
          tenantId);
      return;
    }

    for (EmployeeHandoffRecord employeeRecord : handoff.getEmployeeRecords()) {
      processEmployeeHandoff(employeeRecord, payrollPeriodId, tenantId);
    }

    log.info(
        "event=PRL_ATTENDANCE_HANDOFF_MANUAL_SUCCESS periodId={} tenantId={}", periodId, tenantId);
  }

  @Override
  @Transactional
  public void handle(AttendancePeriodClosedEvent event) {
    Objects.requireNonNull(event, "AttendancePeriodClosedEvent no puede ser nulo");

    log.info(
        "event=PRL_ATTENDANCE_HANDOFF_RECEIVED periodId={} tenantId={} employeeCount={}",
        event.periodId(),
        event.tenantId(),
        event.handoffPackage().getEmployeeRecords().size());

    UUID tenantId = event.tenantId();

    // Resolver el PayrollPeriod de nómina por mes/año del período de TM.
    // El event.periodId() es el UUID del timesheet_period (BC-TM), distinto al
    // UUID de prl_payroll_period que exige la FK de income/deduction records.
    int month = event.periodBoundary().periodStart().getMonthValue();
    int year = event.periodBoundary().periodStart().getYear();
    PayrollPeriod payrollPeriod =
        payrollPeriodRepository
            .findByMonthAndYear(month, year, tenantId)
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "No existe un PayrollPeriod para %d/%d y tenantId=%s"
                            .formatted(month, year, tenantId)));
    UUID payrollPeriodId = payrollPeriod.getPeriodId();

    PayrollHandoffPackage handoff = event.handoffPackage();
    for (EmployeeHandoffRecord employeeRecord : handoff.getEmployeeRecords()) {
      processEmployeeHandoff(employeeRecord, payrollPeriodId, tenantId);
    }

    log.info(
        "event=PRL_ATTENDANCE_HANDOFF_PROCESSED periodId={} payrollPeriodId={} tenantId={}",
        event.periodId(),
        payrollPeriodId,
        tenantId);
  }

  /**
   * Procesa el resumen de un empleado individual, generando los registros de ingresos y egresos
   * automáticos correspondientes.
   *
   * <p><b>Idempotencia:</b> Antes de persistir cada registro verifica si ya existe uno automático
   * del mismo tipo para el empleado y período. Si existe, omite la inserción y registra un log de
   * SKIP. Esto permite reinvocar el endpoint sin generar duplicados.
   */
  private void processEmployeeHandoff(EmployeeHandoffRecord record, UUID periodRef, UUID tenantId) {

    UUID employeeId = record.relationshipId();

    // ── Ingresos por horas extra (diurnas) — recargo 100% sobre costo-hora (Workflow Fase 2.1) ──
    if (record.overtimeHoursTotal().compareTo(BigDecimal.ZERO) > 0) {
      boolean alreadyExists =
          incomeRecordRepository.findByEmployeeAndPeriod(employeeId, periodRef, tenantId).stream()
              .anyMatch(r -> r.isAutomatic() && IncomeType.HORAS_EXTRA.equals(r.getIncomeType()));
      if (alreadyExists) {
        log.info(
            "event=PRL_INCOME_OVERTIME_SKIP_DUPLICATE employeeId={} periodId={}",
            employeeId,
            periodRef);
      } else {
        IncomeRecord overtimeIncome =
            IncomeRecord.createAutomatic(
                employeeId,
                periodRef,
                IncomeType.HORAS_EXTRA,
                record.overtimeHoursTotal(),
                tenantId);
        incomeRecordRepository.save(overtimeIncome);
        log.info(
            "event=PRL_INCOME_OVERTIME_GENERATED employeeId={} hours={} periodId={}",
            employeeId,
            record.overtimeHoursTotal(),
            periodRef);
      }
    }

    // ── Ingresos por recargo dominical/feriado — recargo 100% (Workflow Fase 2.1) ──
    if (record.holidayHoursTotal().compareTo(BigDecimal.ZERO) > 0) {
      boolean alreadyExists =
          incomeRecordRepository.findByEmployeeAndPeriod(employeeId, periodRef, tenantId).stream()
              .anyMatch(
                  r -> r.isAutomatic() && IncomeType.RECARGO_DOMINICAL.equals(r.getIncomeType()));
      if (alreadyExists) {
        log.info(
            "event=PRL_INCOME_HOLIDAY_SKIP_DUPLICATE employeeId={} periodId={}",
            employeeId,
            periodRef);
      } else {
        IncomeRecord holidayIncome =
            IncomeRecord.createAutomatic(
                employeeId,
                periodRef,
                IncomeType.RECARGO_DOMINICAL,
                record.holidayHoursTotal(),
                tenantId);
        incomeRecordRepository.save(holidayIncome);
        log.info(
            "event=PRL_INCOME_HOLIDAY_GENERATED employeeId={} hours={} periodId={}",
            employeeId,
            record.holidayHoursTotal(),
            periodRef);
      }
    }

    // ── Egresos por ausencias injustificadas (Workflow Fase 3.1 / Paso 4.1) ──
    if (record.unjustifiedAbsences() > 0) {
      boolean alreadyExists =
          deductionRecordRepository
              .findByEmployeeAndPeriod(employeeId, periodRef, tenantId)
              .stream()
              .anyMatch(
                  r -> r.isAutomatic() && DeductionType.AUSENCIA.equals(r.getDeductionType()));
      if (alreadyExists) {
        log.info(
            "event=PRL_DEDUCTION_ABSENCE_SKIP_DUPLICATE employeeId={} periodId={}",
            employeeId,
            periodRef);
      } else {
        BigDecimal absenceDays = BigDecimal.valueOf(record.unjustifiedAbsences());
        DeductionRecord absenceDeduction =
            DeductionRecord.createAutomatic(
                employeeId, periodRef, DeductionType.AUSENCIA, absenceDays, tenantId);
        deductionRecordRepository.save(absenceDeduction);
        log.info(
            "event=PRL_DEDUCTION_ABSENCE_GENERATED employeeId={} days={} periodId={}",
            employeeId,
            record.unjustifiedAbsences(),
            periodRef);
      }
    }
  }
}
