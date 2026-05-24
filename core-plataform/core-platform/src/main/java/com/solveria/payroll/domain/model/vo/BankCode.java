package com.solveria.payroll.domain.model.vo;

import java.util.Objects;

/**
 * Value Object: Código de entidad bancaria.
 *
 * <p>Inmutable. No puede ser nulo ni vacío.
 */
public record BankCode(String code) {

  public BankCode {
    Objects.requireNonNull(code, "BankCode.code no puede ser nulo");
    if (code.isBlank()) {
      throw new IllegalArgumentException("BankCode.code no puede estar vacío");
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof BankCode that)) return false;
    return code.equals(that.code);
  }

  @Override
  public int hashCode() {
    return Objects.hash(code);
  }

  @Override
  public String toString() {
    return "BankCode{code='" + code + "'}";
  }
}
