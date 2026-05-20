package com.solveria.core.accruals.application.service;

import com.solveria.core.accruals.application.command.ApproveLeaveCommand;
import com.solveria.core.accruals.application.port.AccrualBalanceRepositoryPort;
import com.solveria.core.accruals.application.usecase.ApproveLeaveUseCase;
import com.solveria.core.accruals.domain.exception.AccrualBalanceNotFoundException;
import com.solveria.core.accruals.domain.model.AccrualBalance;
import com.solveria.core.accruals.domain.policy.LocalizationPolicy;
import org.springframework.stereotype.Service;

@Service
public class ApproveLeaveService implements ApproveLeaveUseCase {

  private final AccrualBalanceRepositoryPort accrualBalanceRepository;

  public ApproveLeaveService(AccrualBalanceRepositoryPort accrualBalanceRepository) {
    this.accrualBalanceRepository = accrualBalanceRepository;
  }

  @Override
  public AccrualBalance handle(ApproveLeaveCommand command) {
    LocalizationPolicy.requireSantaCruz(command.location());
    AccrualBalance balance =
        accrualBalanceRepository
            .findById(command.balanceId())
            .orElseThrow(() -> new AccrualBalanceNotFoundException(command.balanceId()));
    balance.approveLeave(command.transactionId());
    return accrualBalanceRepository.save(balance);
  }
}
