package com.solveria.core.legal.infrastructure.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplianceSnapshotEmbeddable {

  @Column(name = "smn_applied", precision = 15, scale = 2)
  private BigDecimal smnApplied;

  @Column(name = "tax_regime", length = 50)
  private String taxRegime;

  @Column(name = "infocal_active")
  private Boolean infocalActive;
}
