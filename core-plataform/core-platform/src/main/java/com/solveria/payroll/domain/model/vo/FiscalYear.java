package com.solveria.payroll.domain.model.vo;

import java.util.Objects;

/**
 * Value Object: Año fiscal.
 *
 * <p>Inmutable. Valida que el año sea un valor razonable (1900–2199).
 */
public record FiscalYear(int year) {

    public FiscalYear {
        if (year < 1900 || year > 2199) {
            throw new IllegalArgumentException(
                    "FiscalYear fuera de rango permitido [1900-2199]: " + year);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FiscalYear that)) return false;
        return year == that.year;
    }

    @Override
    public int hashCode() {
        return Objects.hash(year);
    }

    @Override
    public String toString() {
        return "FiscalYear{year=" + year + "}";
    }
}
