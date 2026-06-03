package com.solveria.core.accruals.application.service;

import com.solveria.core.accruals.application.command.ApproveLeaveCommand;
import com.solveria.core.accruals.application.port.AccrualBalanceRepositoryPort;
import com.solveria.core.accruals.application.usecase.ApproveLeaveUseCase;
import com.solveria.core.accruals.domain.exception.AccrualBalanceNotFoundException;
import com.solveria.core.accruals.domain.model.AccrualBalance;
import com.solveria.core.accruals.domain.policy.LocalizationPolicy;
import com.solveria.core.security.context.SecurityTenantContext;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApproveLeaveService implements ApproveLeaveUseCase {

  private final AccrualBalanceRepositoryPort accrualBalanceRepository;

  public ApproveLeaveService(AccrualBalanceRepositoryPort accrualBalanceRepository) {
    this.accrualBalanceRepository = accrualBalanceRepository;
  }

  @Override
  @Transactional
  public AccrualBalance handle(ApproveLeaveCommand command) {
    LocalizationPolicy.requireSantaCruz(command.location());
    UUID tenantId = UUID.fromString(SecurityTenantContext.getCurrentTenantId());
    AccrualBalance balance =
        accrualBalanceRepository
            .findById(command.balanceId())
            .orElseThrow(() -> new AccrualBalanceNotFoundException(command.balanceId()));
    balance.approveLeave(command.transactionId(), tenantId);
    return accrualBalanceRepository.save(balance);
  }
}
