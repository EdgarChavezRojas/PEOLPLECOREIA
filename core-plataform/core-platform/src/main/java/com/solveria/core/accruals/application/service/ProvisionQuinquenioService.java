package com.solveria.core.accruals.application.service;

import com.solveria.core.accruals.application.command.ProvisionQuinquenioCommand;
import com.solveria.core.accruals.application.port.BenefitsRepositoryPort;
import com.solveria.core.accruals.application.usecase.ProvisionQuinquenioUseCase;
import com.solveria.core.accruals.domain.model.QuinquenioProvision;
import com.solveria.core.accruals.domain.policy.LocalizationPolicy;
import com.solveria.core.security.context.SecurityTenantContext;
import java.math.BigDecimal;
import java.util.UUID;

public class ProvisionQuinquenioService implements ProvisionQuinquenioUseCase {

  private final BenefitsRepositoryPort benefitsRepository;

  public ProvisionQuinquenioService(BenefitsRepositoryPort benefitsRepository) {
    this.benefitsRepository = benefitsRepository;
  }

  @Override
  public QuinquenioProvision handle(ProvisionQuinquenioCommand command) {
    LocalizationPolicy.requireSantaCruz(command.location());
    UUID tenantId = UUID.fromString(SecurityTenantContext.getCurrentTenantId());

    QuinquenioProvision provision =
        benefitsRepository
            .findQuinquenioByRelationshipId(command.relationshipId())
            .orElseGet(
                () ->
                    QuinquenioProvision.open(command.relationshipId(), BigDecimal.ZERO, tenantId));

    provision.addMonthlyProvision(command.monthlyAmount());
    return benefitsRepository.saveQuinquenio(provision);
  }
}
