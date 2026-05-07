package com.solveria.core.financial.domain.model.vo;

import java.math.BigDecimal;

/**
 * VO: Tasa de contribución a la Gestora Pública. Invariante: DEBE ser exactamente 12.71% (0.1271).
 */
public record ContributionRate(BigDecimal value) {

  /** Tasa fija de aporte patronal a la Gestora Pública de Bolivia. */
  public static final BigDecimal GESTORA_FIXED_RATE = new BigDecimal("0.1271");

  public ContributionRate {
    if (value == null) {
      throw new IllegalArgumentException("ContributionRate no puede ser null");
    }
    if (value.compareTo(GESTORA_FIXED_RATE) != 0) {
      throw new IllegalArgumentException(
          "Deducción Laboral Exacta: la tasa de contribución DEBE ser 12.71% (0.1271). Valor recibido: "
              + value);
    }
  }

  public static ContributionRate gestoraDefault() {
    return new ContributionRate(GESTORA_FIXED_RATE);
  }
}
