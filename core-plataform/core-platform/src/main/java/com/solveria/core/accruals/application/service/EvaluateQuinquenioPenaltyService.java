package com.solveria.core.accruals.application.service;

import com.solveria.core.accruals.application.command.EvaluateQuinquenioPenaltyCommand;
import com.solveria.core.accruals.application.port.BenefitsRepositoryPort;
import com.solveria.core.accruals.application.usecase.EvaluateQuinquenioPenaltyUseCase;
import com.solveria.core.accruals.domain.exception.QuinquenioProvisionNotFoundException;
import com.solveria.core.accruals.domain.model.QuinquenioProvision;
import com.solveria.core.accruals.domain.policy.LocalizationPolicy;
import com.solveria.core.security.context.SecurityTenantContext;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class EvaluateQuinquenioPenaltyService implements EvaluateQuinquenioPenaltyUseCase {

  private final BenefitsRepositoryPort benefitsRepository;

  public EvaluateQuinquenioPenaltyService(BenefitsRepositoryPort benefitsRepository) {
    this.benefitsRepository = benefitsRepository;
  }

  @Override
  public QuinquenioProvision handle(EvaluateQuinquenioPenaltyCommand command) {
    UUID tenantId = UUID.fromString(SecurityTenantContext.getTenantId());
    LocalizationPolicy.requireSantaCruz(command.location());
    QuinquenioProvision provision =
        benefitsRepository
            .findQuinquenioByRelationshipId(command.relationshipId())
            .orElseThrow(() -> new QuinquenioProvisionNotFoundException(command.relationshipId()));
    LocalDate today = command.today() != null ? command.today() : LocalDate.now();
    provision.evaluatePenalty(command.requestDate(), today, command.paymentDate(), tenantId);
    return benefitsRepository.saveQuinquenio(provision);
  }
}
