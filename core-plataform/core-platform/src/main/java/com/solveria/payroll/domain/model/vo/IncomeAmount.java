package com.solveria.payroll.domain.model.vo;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value Object: Monto de un ingreso del período.
 *
 * <p>Inmutable. Debe ser mayor o igual a cero. Envuelve un {@link BigDecimal} para evitar
 * primitivos sueltos.
 */
public record IncomeAmount(BigDecimal value) {

  public IncomeAmount {
    Objects.requireNonNull(value, "IncomeAmount.value no puede ser nulo");
    if (value.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("IncomeAmount.value no puede ser negativo: " + value);
    }
  }

  /**
   * Aplica un porcentaje de penalización sobre este monto.
   *
   * @param penaltyRate porcentaje como fracción decimal (ej. 0.30 para 30%)
   * @return nuevo IncomeAmount con la penalización sumada
   */
  public IncomeAmount applyPenalty(BigDecimal penaltyRate) {
    Objects.requireNonNull(penaltyRate, "penaltyRate no puede ser nulo");
    BigDecimal penalty = value.multiply(penaltyRate);
    return new IncomeAmount(value.add(penalty));
  }
}
