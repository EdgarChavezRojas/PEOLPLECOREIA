package com.solveria.core.accruals.application.service;

import com.solveria.core.accruals.application.command.RegisterSeniorityMilestoneCommand;
import com.solveria.core.accruals.application.port.AccrualBalanceRepositoryPort;
import com.solveria.core.accruals.application.usecase.RegisterSeniorityMilestoneUseCase;
import com.solveria.core.accruals.domain.exception.AccrualBalanceNotFoundException;
import com.solveria.core.accruals.domain.model.AccrualBalance;
import com.solveria.core.accruals.domain.model.vo.SeniorityMilestone;
import com.solveria.core.accruals.domain.policy.LocalizationPolicy;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterSeniorityMilestoneService implements RegisterSeniorityMilestoneUseCase {

  private final AccrualBalanceRepositoryPort accrualBalanceRepository;

  public RegisterSeniorityMilestoneService(AccrualBalanceRepositoryPort accrualBalanceRepository) {
    this.accrualBalanceRepository = accrualBalanceRepository;
  }

  @Override
  @Transactional
  public AccrualBalance handle(RegisterSeniorityMilestoneCommand command) {
    LocalizationPolicy.requireSantaCruz(command.location());
    AccrualBalance balance =
        accrualBalanceRepository
            .findById(command.balanceId())
            .orElseThrow(() -> new AccrualBalanceNotFoundException(command.balanceId()));
    SeniorityMilestone milestone =
        new SeniorityMilestone(UUID.randomUUID(), command.monthsCompleted(), command.baseSmnType());
    balance.addSeniorityMilestone(milestone);
    return accrualBalanceRepository.save(balance);
  }
}
