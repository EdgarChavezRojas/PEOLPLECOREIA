package com.solveria.core.financial.domain.model.vo;

import java.math.BigDecimal;

/** VO: Monto presupuestario con validación de no-negatividad. */
public record BudgetAmount(BigDecimal value) {

  public BudgetAmount {
    if (value == null) {
      throw new IllegalArgumentException("BudgetAmount no puede ser null");
    }
    if (value.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("BudgetAmount no puede ser negativo: " + value);
    }
  }

  public static BudgetAmount of(BigDecimal value) {
    return new BudgetAmount(value);
  }

  public static BudgetAmount zero() {
    return new BudgetAmount(BigDecimal.ZERO);
  }

  public BudgetAmount subtract(BigDecimal amount) {
    BigDecimal result = this.value.subtract(amount);
    if (result.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException(
          "Fondos insuficientes: saldo=" + this.value + ", requerido=" + amount);
    }
    return new BudgetAmount(result);
  }

  public boolean isSufficientFor(BigDecimal requiredAmount) {
    return this.value.compareTo(requiredAmount) >= 0;
  }

  public boolean isExhausted() {
    return this.value.compareTo(BigDecimal.ZERO) == 0;
  }
}
