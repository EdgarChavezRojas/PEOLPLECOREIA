package com.solveria.core.financial.domain.model.vo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * VO: Distribución porcentual del costo laboral a una unidad organizativa. Invariante: La suma de
 * todos los splits de un FundingSource DEBE ser 100%.
 */
public record LaborCostSplit(
    UUID splitId, UUID unitId, BigDecimal percentage, LocalDate effectiveDate) {

  public LaborCostSplit {
    if (splitId == null) {
      throw new IllegalArgumentException("splitId es obligatorio");
    }
    if (unitId == null) {
      throw new IllegalArgumentException("unitId es obligatorio");
    }
    if (percentage == null
        || percentage.compareTo(BigDecimal.ZERO) < 0
        || percentage.compareTo(new BigDecimal("100.00")) > 0) {
      throw new IllegalArgumentException("percentage debe estar entre 0 y 100");
    }
    if (effectiveDate == null) {
      throw new IllegalArgumentException("effectiveDate es obligatorio");
    }
  }

  public static LaborCostSplit create(UUID unitId, BigDecimal percentage, LocalDate effectiveDate) {
    return new LaborCostSplit(UUID.randomUUID(), unitId, percentage, effectiveDate);
  }
}
