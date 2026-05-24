package com.solveria.payroll.domain.model.vo;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value Object: Monto de un egreso/descuento del período.
 *
 * <p>Inmutable. Debe ser mayor o igual a cero. Envuelve un {@link BigDecimal} para evitar
 * primitivos sueltos.
 */
public record DeductionAmount(BigDecimal value) {

  public DeductionAmount {
    Objects.requireNonNull(value, "DeductionAmount.value no puede ser nulo");
    if (value.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("DeductionAmount.value no puede ser negativo: " + value);
    }
  }
}
