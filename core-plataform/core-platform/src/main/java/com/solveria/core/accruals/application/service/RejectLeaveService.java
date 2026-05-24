package com.solveria.core.accruals.application.service;

import com.solveria.core.accruals.application.command.RejectLeaveCommand;
import com.solveria.core.accruals.application.port.AccrualBalanceRepositoryPort;
import com.solveria.core.accruals.application.usecase.RejectLeaveUseCase;
import com.solveria.core.accruals.domain.exception.AccrualBalanceNotFoundException;
import com.solveria.core.accruals.domain.model.AccrualBalance;
import com.solveria.core.accruals.domain.policy.LocalizationPolicy;
import org.springframework.stereotype.Service;

@Service
public class RejectLeaveService implements RejectLeaveUseCase {

  private final AccrualBalanceRepositoryPort accrualBalanceRepository;

  public RejectLeaveService(AccrualBalanceRepositoryPort accrualBalanceRepository) {
    this.accrualBalanceRepository = accrualBalanceRepository;
  }

  @Override
  public AccrualBalance handle(RejectLeaveCommand command) {
    LocalizationPolicy.requireSantaCruz(command.location());
    AccrualBalance balance =
        accrualBalanceRepository
            .findById(command.balanceId())
            .orElseThrow(() -> new AccrualBalanceNotFoundException(command.balanceId()));
    balance.rejectLeave(command.transactionId());
    return accrualBalanceRepository.save(balance);
  }
}
