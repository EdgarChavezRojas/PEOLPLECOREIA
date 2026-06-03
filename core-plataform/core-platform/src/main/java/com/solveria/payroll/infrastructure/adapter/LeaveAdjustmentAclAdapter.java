package com.solveria.payroll.infrastructure.adapter;

import com.solveria.core.accruals.infrastructure.jpa.LeaveTransactionJpa;
import com.solveria.core.accruals.infrastructure.repository.LeaveTransactionRepository;
import com.solveria.payroll.application.port.outbound.LeaveAdjustmentAclPort;
import com.solveria.payroll.infrastructure.jpa.PayrollPeriodJpa;
import com.solveria.payroll.infrastructure.repository.PayrollPeriodSpringRepository;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Adaptador de Infraestructura para el puerto LeaveAdjustmentAclPort.
 *
 * <p>Implementa la resolución de entidades externas utilizando repositorios de Spring Data JPA,
 * adhiriéndose completamente a los principios de arquitectura limpia/hexagonal.
 */
@Component
@RequiredArgsConstructor
public class LeaveAdjustmentAclAdapter implements LeaveAdjustmentAclPort {

  private final LeaveTransactionRepository leaveTransactionRepository;
  private final PayrollPeriodSpringRepository payrollPeriodSpringRepository;

  @Override
  public UUID getRelationshipIdByTransactionId(UUID transactionId) {
    if (transactionId == null) {
      return null;
    }
    return leaveTransactionRepository
        .findById(transactionId)
        .map(LeaveTransactionJpa::getBalance)
        .map(com.solveria.core.accruals.infrastructure.jpa.AccrualBalanceJpa::getRelationshipId)
        .orElse(null);
  }

  @Override
  public UUID getLatestPeriodId(UUID tenantId) {
    if (tenantId == null) {
      return null;
    }
    List<PayrollPeriodJpa> periods = payrollPeriodSpringRepository.findAllByTenantId(tenantId);
    if (periods.isEmpty()) {
      // Fallback: buscar cualquier periodo disponible
      periods = payrollPeriodSpringRepository.findAll();
    }
    return periods.stream()
        .max(
            Comparator.comparing(PayrollPeriodJpa::getYear)
                .thenComparing(PayrollPeriodJpa::getMonth))
        .map(PayrollPeriodJpa::getPeriodId)
        .orElse(null);
  }
}
