package com.solveria.payroll.domain.model.vo;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Value Object: Fecha de corte de nómina.
 *
 * <p>Inmutable. No puede ser nula.
 */
public record CutoffDate(LocalDate date) {

  public CutoffDate {
    Objects.requireNonNull(date, "CutoffDate.date no puede ser nulo");
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof CutoffDate that)) return false;
    return date.equals(that.date);
  }

  @Override
  public int hashCode() {
    return Objects.hash(date);
  }

  @Override
  public String toString() {
    return "CutoffDate{date=" + date + "}";
  }
}
