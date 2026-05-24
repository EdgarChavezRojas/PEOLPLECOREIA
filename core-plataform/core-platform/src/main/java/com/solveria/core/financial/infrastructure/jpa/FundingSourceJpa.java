package com.solveria.core.financial.infrastructure.jpa;

import com.solveria.core.shared.base.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** JPA Entity: FundingSource (Aggregate Root). Tabla: funding_source. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "funding_source")
public class FundingSourceJpa extends BaseEntity {
  @Id
  @Column(name = "source_id", nullable = false, unique = true, updatable = false)
  private UUID sourceId;

  @Column(name = "project_code", length = 50, nullable = false)
  private String projectCode;

  @Column(name = "total_budget", precision = 18, scale = 2, nullable = false)
  private BigDecimal totalBudget;

  @Column(name = "available_budget", precision = 18, scale = 2, nullable = false)
  private BigDecimal availableBudget;

  @Column(name = "burn_rate", precision = 5, scale = 2)
  private BigDecimal burnRate;

  @Column(name = "created_by_user")
  private String createdByUser;

  @OneToMany(
      mappedBy = "fundingSource",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private List<LaborCostSplitJpa> costSplits = new ArrayList<>();
}
