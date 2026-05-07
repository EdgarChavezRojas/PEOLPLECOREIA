package com.solveria.core.legal.infrastructure.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.time.LocalDate;
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
public class LegalThresholdEmbeddable {

  @Column(name = "threshold_value", precision = 15, scale = 4)
  private BigDecimal thresholdValue;

  @Column(name = "effective_date")
  private LocalDate effectiveDate;
}
