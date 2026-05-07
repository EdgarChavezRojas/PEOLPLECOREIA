package com.solveria.core.accruals.application.service;

import com.solveria.core.accruals.application.command.CalculateQuinquenioCommand;
import com.solveria.core.accruals.application.port.BenefitsRepositoryPort;
import com.solveria.core.accruals.application.usecase.CalculateQuinquenioUseCase;
import com.solveria.core.accruals.domain.model.QuinquenioProvision;
import com.solveria.core.accruals.domain.policy.LocalizationPolicy;
import com.solveria.core.accruals.domain.policy.QuinquenioPolicy;
import com.solveria.core.accruals.domain.policy.SeniorityBasePolicy;
import com.solveria.core.security.context.SecurityTenantContext;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class CalculateQuinquenioService implements CalculateQuinquenioUseCase {

  private final BenefitsRepositoryPort benefitsRepository;

  public CalculateQuinquenioService(BenefitsRepositoryPort benefitsRepository) {
    this.benefitsRepository = benefitsRepository;
  }

  @Override
  public QuinquenioProvision handle(CalculateQuinquenioCommand command) {
    LocalizationPolicy.requireSantaCruz(command.location());
    UUID tenantId = UUID.fromString(SecurityTenantContext.getCurrentTenantId());

    QuinquenioProvision provision =
        benefitsRepository
            .findQuinquenioByRelationshipId(command.relationshipId())
            .orElseGet(
                () ->
                    QuinquenioProvision.open(command.relationshipId(), BigDecimal.ZERO, tenantId));

    if (QuinquenioPolicy.isEligible(command.monthsCompleted())) {
      provision.markEligible();
    }

    if (command.requestDate() != null) {
      provision.requestPayment(command.requestDate());
    }

    if (command.averageLast90Days() != null) {
      provision.finalizeCalculation(command.averageLast90Days());
    } else {
      BigDecimal base = SeniorityBasePolicy.resolveBaseAmount(command.tenantSegment());
      provision.finalizeCalculation(base);
    }

    LocalDate today = command.today() != null ? command.today() : LocalDate.now();
    provision.evaluatePenalty(command.requestDate(), today, command.paymentDate());

    if (command.paymentDate() != null) {
      provision.markPaid(command.paymentDate());
    }

    return benefitsRepository.saveQuinquenio(provision);
  }
}
