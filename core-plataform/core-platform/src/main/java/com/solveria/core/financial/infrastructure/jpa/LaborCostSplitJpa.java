package com.solveria.core.financial.infrastructure.jpa;

import com.solveria.core.shared.base.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** JPA Entity: LaborCostSplit (VO persistido como tabla hija). Tabla: labor_cost_split. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "labor_cost_split")
public class LaborCostSplitJpa extends BaseEntity {
  @Id
  @Column(name = "split_id", nullable = false, unique = true, updatable = false)
  private UUID splitId;

  @Column(name = "unit_id", nullable = false)
  private UUID unitId;

  @Column(name = "percentage", precision = 5, scale = 2, nullable = false)
  private BigDecimal percentage;

  @Column(name = "effective_date", nullable = false)
  private LocalDate effectiveDate;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "funding_source_id", nullable = false)
  private FundingSourceJpa fundingSource;
}
