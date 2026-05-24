package com.solveria.core.accruals.application.service;

import com.solveria.core.accruals.application.command.ProvisionBenefitBatchCommand;
import com.solveria.core.accruals.application.port.BenefitsRepositoryPort;
import com.solveria.core.accruals.application.usecase.ProvisionBenefitBatchUseCase;
import com.solveria.core.accruals.domain.model.BenefitAccrual;
import com.solveria.core.accruals.domain.policy.LocalizationPolicy;
import com.solveria.core.security.context.SecurityTenantContext;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ProvisionBenefitBatchService implements ProvisionBenefitBatchUseCase {

  private final BenefitsRepositoryPort benefitsRepository;

  public ProvisionBenefitBatchService(BenefitsRepositoryPort benefitsRepository) {
    this.benefitsRepository = benefitsRepository;
  }

  @Override
  public List<BenefitAccrual> handle(ProvisionBenefitBatchCommand command) {
    LocalizationPolicy.requireSantaCruz(command.location());
    UUID tenantId = UUID.fromString(SecurityTenantContext.getCurrentTenantId());
    List<BenefitAccrual> accruals = new ArrayList<>();

    for (ProvisionBenefitBatchCommand.BenefitProvisionItem item : command.items()) {
      BenefitAccrual accrual =
          benefitsRepository
              .findBenefitAccrual(item.employeeId(), item.benefitType(), item.fiscalYear())
              .orElseGet(
                  () ->
                      BenefitAccrual.open(
                          item.employeeId(),
                          item.benefitType(),
                          item.fiscalYear(),
                          BigDecimal.ZERO,
                          tenantId));
      accrual.addAccrual(item.amount());
      accruals.add(accrual);
    }

    return benefitsRepository.saveBenefitAccrualBatch(accruals);
  }
}
