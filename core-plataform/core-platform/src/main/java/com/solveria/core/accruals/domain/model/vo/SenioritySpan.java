package com.solveria.core.accruals.domain.model.vo;

import java.time.LocalDate;
import java.time.Period;

/**
 * Value Object: Antigüedad desglosada en años, meses y días. Los reportes de RRHH exigen visualizar
 * la antigüedad exacta desglosada. Este VO permite exponer el dato sin realizar cálculos pesados en
 * la interfaz.
 */
public record SenioritySpan(int years, int months, int days) {

  /**
   * Calcula la antigüedad entre la fecha de contratación y una fecha de referencia.
   *
   * @param hireDate Fecha de inicio de la relación laboral
   * @param referenceDate Fecha de referencia (normalmente hoy)
   * @return SenioritySpan con el desglose exacto
   */
  public static SenioritySpan between(LocalDate hireDate, LocalDate referenceDate) {
    if (hireDate == null || referenceDate == null) {
      return new SenioritySpan(0, 0, 0);
    }
    Period period = Period.between(hireDate, referenceDate);
    return new SenioritySpan(period.getYears(), period.getMonths(), period.getDays());
  }

  /** Total de meses completos de antigüedad (para cálculo de quinquenio P8). */
  public int totalMonths() {
    return (years * 12) + months;
  }
}
