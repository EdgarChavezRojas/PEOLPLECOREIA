package com.solveria.core.accruals.domain.model;

import com.solveria.core.accruals.domain.model.vo.BenefitType;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BenefitAccrual {

  private UUID benefitId;
  private UUID relationshipId;
  private BenefitType benefitType;
  private int fiscalYear;
  private BigDecimal accruedAmount;
  private UUID tenantId;

  public static BenefitAccrual open(
      UUID relationshipId,
      BenefitType benefitType,
      int fiscalYear,
      BigDecimal accruedAmount,
      UUID tenantId) {
    if (relationshipId == null) {
      throw new IllegalArgumentException("relationshipId is required");
    }
    if (benefitType == null) {
      throw new IllegalArgumentException("benefitType is required");
    }
    if (fiscalYear <= 0) {
      throw new IllegalArgumentException("fiscalYear must be positive");
    }
    if (accruedAmount == null) {
      throw new IllegalArgumentException("accruedAmount is required");
    }
    if (tenantId == null) {
      throw new IllegalArgumentException("tenantId is required");
    }
    return BenefitAccrual.builder()
        .benefitId(UUID.randomUUID())
        .relationshipId(relationshipId)
        .benefitType(benefitType)
        .fiscalYear(fiscalYear)
        .accruedAmount(accruedAmount)
        .tenantId(tenantId)
        .build();
  }

  public void addAccrual(BigDecimal amount) {
    if (amount == null || amount.signum() <= 0) {
      throw new IllegalArgumentException("amount must be positive");
    }
    accruedAmount = accruedAmount.add(amount);
  }
}
