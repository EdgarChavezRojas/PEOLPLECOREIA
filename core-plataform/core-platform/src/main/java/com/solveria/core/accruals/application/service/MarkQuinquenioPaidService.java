package com.solveria.core.accruals.application.service;

import com.solveria.core.accruals.application.command.MarkQuinquenioPaidCommand;
import com.solveria.core.accruals.application.port.BenefitsRepositoryPort;
import com.solveria.core.accruals.application.usecase.MarkQuinquenioPaidUseCase;
import com.solveria.core.accruals.domain.exception.QuinquenioProvisionNotFoundException;
import com.solveria.core.accruals.domain.model.QuinquenioProvision;
import com.solveria.core.accruals.domain.policy.LocalizationPolicy;
import org.springframework.stereotype.Service;

@Service
public class MarkQuinquenioPaidService implements MarkQuinquenioPaidUseCase {

  private final BenefitsRepositoryPort benefitsRepository;

  public MarkQuinquenioPaidService(BenefitsRepositoryPort benefitsRepository) {
    this.benefitsRepository = benefitsRepository;
  }

  @Override
  public QuinquenioProvision handle(MarkQuinquenioPaidCommand command) {
    LocalizationPolicy.requireSantaCruz(command.location());
    QuinquenioProvision provision =
        benefitsRepository
            .findQuinquenioByRelationshipId(command.relationshipId())
            .orElseThrow(() -> new QuinquenioProvisionNotFoundException(command.relationshipId()));
    provision.markPaid(command.paymentDate());
    return benefitsRepository.saveQuinquenio(provision);
  }
}
