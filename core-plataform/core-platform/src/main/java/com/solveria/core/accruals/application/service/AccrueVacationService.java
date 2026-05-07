package com.solveria.core.accruals.application.service;

import com.solveria.core.accruals.application.command.AccrueVacationCommand;
import com.solveria.core.accruals.application.port.AccrualBalanceRepositoryPort;
import com.solveria.core.accruals.application.usecase.AccrueVacationUseCase;
import com.solveria.core.accruals.domain.exception.AccrualBalanceNotFoundException;
import com.solveria.core.accruals.domain.model.AccrualBalance;
import com.solveria.core.accruals.domain.policy.LocalizationPolicy;

public class AccrueVacationService implements AccrueVacationUseCase {

  private final AccrualBalanceRepositoryPort accrualBalanceRepository;

  public AccrueVacationService(AccrualBalanceRepositoryPort accrualBalanceRepository) {
    this.accrualBalanceRepository = accrualBalanceRepository;
  }

  @Override
  public AccrualBalance handle(AccrueVacationCommand command) {
    LocalizationPolicy.requireSantaCruz(command.location());
    AccrualBalance balance =
        accrualBalanceRepository
            .findById(command.balanceId())
            .orElseThrow(() -> new AccrualBalanceNotFoundException(command.balanceId()));
    balance.accrueVacation(command.yearsOfService(), command.accrualDate());
    return accrualBalanceRepository.save(balance);
  }
}
