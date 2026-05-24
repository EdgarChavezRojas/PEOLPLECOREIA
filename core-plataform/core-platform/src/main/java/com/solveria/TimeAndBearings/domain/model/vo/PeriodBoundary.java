package com.solveria.TimeAndBearings.domain.model.vo;

import com.solveria.TimeAndBearings.domain.model.enums.PeriodType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * VO inmutable que define los límites temporales de un {@code TimesheetPeriod} (Aggregate 16).
 *
 * <p><b>Atributos (según diccionario de datos):</b>
 *
 * <ul>
 *   <li>{@code periodStart} – Primer día del periodo. NOT NULL.
 *   <li>{@code periodEnd} – Último día del periodo. Invariante: &gt; periodStart. NOT NULL.
 *   <li>{@code periodType} – WEEKLY / BIWEEKLY / MONTHLY. Configurado a nivel Tenant.
 *   <li>{@code gracePeriodEnd} – Calculado: period_end + días hábiles según P-TM34. NOT NULL.
 * </ul>
 *
 * <p><b>Invariante de dominio:</b> {@code periodEnd} debe ser estrictamente posterior a {@code
 * periodStart}; {@code gracePeriodEnd} debe ser posterior a {@code periodEnd}. El constructor
 * valida ambas condiciones lanzando {@link IllegalArgumentException} si se violan (guard clause de
 * dominio puro).
 */
public record PeriodBoundary(
    LocalDate periodStart,
    LocalDate periodEnd,
    PeriodType periodType,
    LocalDateTime gracePeriodEnd) {

  /** Guard clause: valida invariantes de consistencia temporal en construcción. */
  public PeriodBoundary {
    Objects.requireNonNull(periodStart, "periodStart es requerido");
    Objects.requireNonNull(periodEnd, "periodEnd es requerido");
    Objects.requireNonNull(periodType, "periodType es requerido");
    Objects.requireNonNull(gracePeriodEnd, "gracePeriodEnd es requerido");

    if (!periodEnd.isAfter(periodStart)) {
      throw new IllegalArgumentException(
          "periodEnd [%s] debe ser posterior a periodStart [%s]".formatted(periodEnd, periodStart));
    }
    if (!gracePeriodEnd.isAfter(periodEnd.atStartOfDay())) {
      throw new IllegalArgumentException(
          "gracePeriodEnd [%s] debe ser posterior a periodEnd [%s]"
              .formatted(gracePeriodEnd, periodEnd));
    }
  }

  /**
   * Verifica si el periodo de gracia ya venció según P-TM34.
   *
   * @param now timestamp actual del servidor NTP
   * @return {@code true} si {@code now} es posterior a {@code gracePeriodEnd}
   */
  public boolean isGracePeriodExpired(LocalDateTime now) {
    return now.isAfter(gracePeriodEnd);
  }

  /**
   * Verifica si la fecha dada está dentro del Periodo de Gracia (posterior a {@code periodEnd} pero
   * anterior/igual a {@code gracePeriodEnd}).
   *
   * @param now timestamp actual del servidor NTP
   * @return {@code true} si está en el periodo de gracia
   */
  public boolean isInGracePeriod(LocalDateTime now) {
    LocalDateTime periodEndTime = periodEnd.atStartOfDay();
    return now.isAfter(periodEndTime) && !now.isAfter(gracePeriodEnd);
  }
}
