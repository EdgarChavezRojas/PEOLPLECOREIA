package com.solveria.core.accruals.application.service;

import com.solveria.core.accruals.application.command.OpenAccrualBalanceCommand;
import com.solveria.core.accruals.application.port.AccrualBalanceRepositoryPort;
import com.solveria.core.accruals.application.usecase.OpenAccrualBalanceUseCase;
import com.solveria.core.accruals.domain.model.AccrualBalance;
import com.solveria.core.accruals.domain.policy.LocalizationPolicy;
import com.solveria.core.security.context.SecurityTenantContext;
import org.springframework.stereotype.Service;

import java.util.UUID;
@Service
public class OpenAccrualBalanceService implements OpenAccrualBalanceUseCase {

  private final AccrualBalanceRepositoryPort accrualBalanceRepository;

  public OpenAccrualBalanceService(AccrualBalanceRepositoryPort accrualBalanceRepository) {
    this.accrualBalanceRepository = accrualBalanceRepository;
  }

  @Override
  public AccrualBalance handle(OpenAccrualBalanceCommand command) {
    LocalizationPolicy.requireSantaCruz(command.location());
    UUID tenantId = UUID.fromString(SecurityTenantContext.getCurrentTenantId());
    AccrualBalance balance =
        AccrualBalance.open(
            command.relationshipId(),
            command.balanceType(),
            command.unit(),
            command.currentBalance(),
            command.lastAccrualDate(),
            tenantId);
    return accrualBalanceRepository.save(balance);
  }
}

