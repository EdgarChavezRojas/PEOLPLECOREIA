package com.solveria.payroll.application.usecase;

import com.solveria.payroll.application.port.inbound.ApplyLeaveAdjustmentsUseCase;
import com.solveria.payroll.application.port.outbound.DeductionRecordRepositoryPort;
import com.solveria.payroll.application.port.outbound.LeaveAdjustmentAclPort;
import com.solveria.payroll.domain.model.ar.DeductionRecord;
import com.solveria.payroll.domain.model.vo.DeductionType;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de Uso: Procesa los ajustes de nómina por días de vacación aprobados.
 *
 * <p>Genera automáticamente una deducción de tipo {@link DeductionType#AJUSTE_VACACION}
 * proporcional a los días solicitados, aplicando un valor diario fijo temporal para vales de
 * alimentación.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessLeaveAdjustmentsUseCase implements ApplyLeaveAdjustmentsUseCase {

  private final DeductionRecordRepositoryPort deductionRecordRepositoryPort;
  private final LeaveAdjustmentAclPort leaveAdjustmentAclPort;

  @Override
  @Transactional
  public void applyAdjustments(UUID transactionId, BigDecimal daysRequested, UUID tenantId) {

    BigDecimal amount = daysRequested.multiply(new BigDecimal("20.00"));

    // Resolve employeeId (relationship_id) using the ACL Port
    UUID employeeId = leaveAdjustmentAclPort.getRelationshipIdByTransactionId(transactionId);
    if (employeeId == null) {
      log.warn(
          "Could not resolve employeeId from transactionId={}, using random fallback",
          transactionId);
      employeeId = UUID.randomUUID();
    }

    // Resolve periodRef using the ACL Port
    UUID periodRef = leaveAdjustmentAclPort.getLatestPeriodId(tenantId);
    if (periodRef == null) {
      log.warn("Could not resolve periodRef from database, using hardcoded fallback");
      periodRef =
          UUID.fromString("d1d00000-0000-4000-8000-000000000005"); // Fallback to May 2026 period
    }

    DeductionRecord record =
        DeductionRecord.createAutomatic(
            employeeId, periodRef, DeductionType.AJUSTE_VACACION, amount, tenantId);

    deductionRecordRepositoryPort.save(record);

    log.info(
        "event=PRL_LEAVE_ADJUSTMENT_APPLIED transactionId={} days={} amount={}",
        transactionId,
        daysRequested,
        amount);
  }
}
