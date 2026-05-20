package com.solveria.core.accruals.application.command;

import com.solveria.core.accruals.domain.model.vo.BenefitType;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProvisionBenefitBatchCommand(List<BenefitProvisionItem> items, String location) {

  public record BenefitProvisionItem(
      UUID employeeId, BenefitType benefitType, int fiscalYear, BigDecimal amount) {}
}
