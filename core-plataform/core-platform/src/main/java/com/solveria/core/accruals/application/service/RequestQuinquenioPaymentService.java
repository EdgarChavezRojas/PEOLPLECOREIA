package com.solveria.core.accruals.application.service;

import com.solveria.core.accruals.application.command.RequestQuinquenioPaymentCommand;
import com.solveria.core.accruals.application.port.BenefitsRepositoryPort;
import com.solveria.core.accruals.application.usecase.RequestQuinquenioPaymentUseCase;
import com.solveria.core.accruals.domain.exception.QuinquenioProvisionNotFoundException;
import com.solveria.core.accruals.domain.model.QuinquenioProvision;
import com.solveria.core.accruals.domain.policy.LocalizationPolicy;
import org.springframework.stereotype.Service;

@Service
public class RequestQuinquenioPaymentService implements RequestQuinquenioPaymentUseCase {

  private final BenefitsRepositoryPort benefitsRepository;

  public RequestQuinquenioPaymentService(BenefitsRepositoryPort benefitsRepository) {
    this.benefitsRepository = benefitsRepository;
  }

  @Override
  public QuinquenioProvision handle(RequestQuinquenioPaymentCommand command) {
    LocalizationPolicy.requireSantaCruz(command.location());
    QuinquenioProvision provision =
        benefitsRepository
            .findQuinquenioByRelationshipId(command.relationshipId())
            .orElseThrow(() -> new QuinquenioProvisionNotFoundException(command.relationshipId()));
    provision.requestPayment(command.requestDate());
    return benefitsRepository.saveQuinquenio(provision);
  }
}
