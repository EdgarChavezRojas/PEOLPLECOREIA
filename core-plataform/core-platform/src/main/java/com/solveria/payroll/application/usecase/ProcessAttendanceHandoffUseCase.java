package com.solveria.payroll.application.usecase;

import com.solveria.TimeAndBearings.domain.event.AttendancePeriodClosedEvent;
import com.solveria.TimeAndBearings.domain.model.entity.PayrollHandoffPackage;
import com.solveria.TimeAndBearings.domain.model.vo.EmployeeHandoffRecord;
import com.solveria.payroll.application.port.inbound.AttendanceHandoffUseCase;
import com.solveria.payroll.application.port.outbound.DeductionRecordRepositoryPort;
import com.solveria.payroll.application.port.outbound.IncomeRecordRepositoryPort;
import com.solveria.payroll.domain.model.ar.DeductionRecord;
import com.solveria.payroll.domain.model.ar.IncomeRecord;
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

  @Override
  @Transactional
  public void handle(AttendancePeriodClosedEvent event) {
    Objects.requireNonNull(event, "AttendancePeriodClosedEvent no puede ser nulo");

    log.info(
        "event=PRL_ATTENDANCE_HANDOFF_RECEIVED periodId={} tenantId={} employeeCount={}",
        event.periodId(),
        event.tenantId(),
        event.handoffPackage().getEmployeeRecords().size());

    PayrollHandoffPackage handoff = event.handoffPackage();
    UUID periodRef = event.periodId();
    UUID tenantId = event.tenantId();

    for (EmployeeHandoffRecord employeeRecord : handoff.getEmployeeRecords()) {
      processEmployeeHandoff(employeeRecord, periodRef, tenantId);
    }

    log.info("event=PRL_ATTENDANCE_HANDOFF_PROCESSED periodId={} tenantId={}", periodRef, tenantId);
  }

  /**
   * Procesa el resumen de un empleado individual, generando los registros de ingresos y egresos
   * automáticos correspondientes.
   */
  private void processEmployeeHandoff(EmployeeHandoffRecord record, UUID periodRef, UUID tenantId) {

    UUID employeeId = record.relationshipId();

    // ── Ingresos por horas extra (diurnas) — recargo 100% sobre costo-hora (Workflow Fase 2.1) ──
    if (record.overtimeHoursTotal().compareTo(BigDecimal.ZERO) > 0) {
      IncomeRecord overtimeIncome =
          IncomeRecord.createAutomatic(
              employeeId, periodRef, IncomeType.HORAS_EXTRA, record.overtimeHoursTotal(), tenantId);
      incomeRecordRepository.save(overtimeIncome);

      log.info(
          "event=PRL_INCOME_OVERTIME_GENERATED employeeId={} hours={} periodId={}",
          employeeId,
          record.overtimeHoursTotal(),
          periodRef);
    }

    // ── Ingresos por recargo dominical/feriado — recargo 100% (Workflow Fase 2.1) ──
    if (record.holidayHoursTotal().compareTo(BigDecimal.ZERO) > 0) {
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

    // ── Egresos por ausencias injustificadas (Workflow Fase 3.1 / Paso 4.1) ──
    if (record.unjustifiedAbsences() > 0) {
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
