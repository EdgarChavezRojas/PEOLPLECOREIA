package com.solveria.core.accruals.domain.model;

import com.solveria.core.accruals.domain.model.vo.BenefitType;
import java.math.BigDecimal;
import java.util.UUID;

public class BenefitAccrual {

  private UUID benefitId;
  private UUID relationshipId;
  private BenefitType benefitType;
  private int fiscalYear;
  private BigDecimal accruedAmount;
  private UUID tenantId;

  public BenefitAccrual() {
  }

  public BenefitAccrual(UUID benefitId, UUID relationshipId, BenefitType benefitType, int fiscalYear, BigDecimal accruedAmount, UUID tenantId) {
    this.benefitId = benefitId;
    this.relationshipId = relationshipId;
    this.benefitType = benefitType;
    this.fiscalYear = fiscalYear;
    this.accruedAmount = accruedAmount;
    this.tenantId = tenantId;
  }

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
    return new BenefitAccrual(
            UUID.randomUUID(),
            relationshipId,
            benefitType,
            fiscalYear,
            accruedAmount,
            tenantId
    );
  }

  public void addAccrual(BigDecimal amount) {
    if (amount == null || amount.signum() <= 0) {
      throw new IllegalArgumentException("amount must be positive");
    }
    accruedAmount = accruedAmount.add(amount);
  }

  // Getters
  public UUID getBenefitId() { return benefitId; }
  public UUID getRelationshipId() { return relationshipId; }
  public BenefitType getBenefitType() { return benefitType; }
  public int getFiscalYear() { return fiscalYear; }
  public BigDecimal getAccruedAmount() { return accruedAmount; }
  public UUID getTenantId() { return tenantId; }
}