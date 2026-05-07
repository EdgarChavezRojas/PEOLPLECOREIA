package com.solveria.core.accruals.infrastructure.jpa;

import com.solveria.core.accruals.domain.model.vo.BenefitType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "benefit_accrual",
    indexes = {
      @Index(name = "idx_benefit_accrual_type_year", columnList = "benefit_type, fiscal_year"),
      @Index(name = "idx_benefit_accrual_tenant", columnList = "tenant_id")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BenefitAccrualJpa {

  @Id
  @Column(name = "benefit_id")
  private UUID benefitId;

  @Enumerated(EnumType.STRING)
  @Column(name = "benefit_type", nullable = false)
  private BenefitType benefitType;

  @Column(name = "fiscal_year", nullable = false)
  private int fiscalYear;

  @Column(name = "accrued_amount", nullable = false)
  private BigDecimal accruedAmount;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;
}
