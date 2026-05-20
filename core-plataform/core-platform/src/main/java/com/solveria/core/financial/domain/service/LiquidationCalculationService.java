package com.solveria.core.financial.domain.service;

import com.solveria.core.financial.domain.model.vo.IndemnizableTrimSnapshot;
import com.solveria.core.financial.domain.model.vo.TerminationType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Servicio de Dominio: Cálculo de Liquidación (Finiquito). Política P15: Promedio de últimos 3
 * meses (Total Ganado) para Aguinaldos/Finiquitos. Política P17: Plazo máximo 15 días (multa 30%).
 * Desahucio si Despido sin Causa.
 *
 * <p>Clase pura sin anotaciones de infraestructura.
 */
public final class LiquidationCalculationService {

  /** Días máximos para pago de finiquito antes de multa (P17). */
  public static final int FINIQUITO_MAX_DAYS = 15;

  /** Días máximos para pago de quinquenio antes de multa. */
  public static final int QUINQUENIO_MAX_DAYS = 30;

  /** Provisión de Aguinaldo: 8.33% mensual. */
  private static final BigDecimal AGUINALDO_RATE = new BigDecimal("0.0833");

  /** Meses para el promedio indemnizable (P15). */
  private static final int AVERAGE_MONTHS = 3;

  private LiquidationCalculationService() {
    // Utility class
  }

  /**
   * Calcula el promedio indemnizable de los últimos 3 meses. (Política P15: Promedio de Total
   * Ganado)
   *
   * @param lastThreeMonthsSalaries Lista de Total Ganado de los últimos 3 meses
   * @return Promedio indemnizable
   */
  public static BigDecimal calculateAverageSalary(List<BigDecimal> lastThreeMonthsSalaries) {
    if (lastThreeMonthsSalaries == null || lastThreeMonthsSalaries.isEmpty()) {
      return BigDecimal.ZERO;
    }
    int count = Math.min(lastThreeMonthsSalaries.size(), AVERAGE_MONTHS);
    BigDecimal sum =
        lastThreeMonthsSalaries.stream().limit(count).reduce(BigDecimal.ZERO, BigDecimal::add);
    return sum.divide(new BigDecimal(count), 2, RoundingMode.HALF_UP);
  }

  /**
   * Calcula la indemnización por años de servicio. 1 salario promedio por año trabajado
   * (proporcional).
   */
  public static BigDecimal calculateIndemnizacion(
      BigDecimal averageSalary, LocalDate startDate, LocalDate endDate) {
    if (averageSalary == null || startDate == null || endDate == null) {
      return BigDecimal.ZERO;
    }
    long totalDays = ChronoUnit.DAYS.between(startDate, endDate);
    BigDecimal years =
        new BigDecimal(totalDays).divide(new BigDecimal("365.25"), 6, RoundingMode.HALF_UP);
    return averageSalary.multiply(years).setScale(2, RoundingMode.HALF_UP);
  }

  /** Calcula el aguinaldo proporcional (8.33% mensual sobre el promedio). */
  public static BigDecimal calculateAguinaldoProporcional(
      BigDecimal averageSalary, LocalDate startDate, LocalDate endDate) {
    if (averageSalary == null || startDate == null || endDate == null) {
      return BigDecimal.ZERO;
    }
    long totalDays = ChronoUnit.DAYS.between(startDate, endDate);
    BigDecimal months =
        new BigDecimal(totalDays).divide(new BigDecimal("30"), 6, RoundingMode.HALF_UP);
    return averageSalary
        .multiply(AGUINALDO_RATE)
        .multiply(months)
        .setScale(2, RoundingMode.HALF_UP);
  }

  /**
   * Calcula vacaciones pendientes. Bolivia: 15 días laborables por año (escalando con antigüedad).
   */
  public static BigDecimal calculateVacacionesPendientes(
      BigDecimal dailySalary, int pendingVacationDays) {
    if (dailySalary == null || pendingVacationDays <= 0) {
      return BigDecimal.ZERO;
    }
    return dailySalary
        .multiply(new BigDecimal(pendingVacationDays))
        .setScale(2, RoundingMode.HALF_UP);
  }

  /** Calcula el total del finiquito incluyendo Desahucio si corresponde. */
  public static BigDecimal calculateTotalLiquidation(
      BigDecimal indemnizacion,
      BigDecimal aguinaldoProporcional,
      BigDecimal vacaciones,
      BigDecimal desahucio,
      TerminationType terminationType) {
    BigDecimal total = BigDecimal.ZERO;
    total = total.add(indemnizacion != null ? indemnizacion : BigDecimal.ZERO);
    total = total.add(aguinaldoProporcional != null ? aguinaldoProporcional : BigDecimal.ZERO);
    total = total.add(vacaciones != null ? vacaciones : BigDecimal.ZERO);

    if (terminationType == TerminationType.DESPIDO_SIN_CAUSA && desahucio != null) {
      total = total.add(desahucio);
    }

    return total.setScale(2, RoundingMode.HALF_UP);
  }

  /** Determina si la multa por mora del finiquito aplica (> 15 días calendario). */
  public static boolean isFiniquitoOverdue(LocalDate terminationDate, LocalDate currentDate) {
    if (terminationDate == null || currentDate == null) {
      return false;
    }
    long daysSinceTermination = ChronoUnit.DAYS.between(terminationDate, currentDate);
    return daysSinceTermination > FINIQUITO_MAX_DAYS;
  }

  /** Determina si la multa por mora del quinquenio aplica (> 30 días calendario). */
  public static boolean isQuinquenioOverdue(LocalDate dueDate, LocalDate currentDate) {
    if (dueDate == null || currentDate == null) {
      return false;
    }
    long daysSinceDue = ChronoUnit.DAYS.between(dueDate, currentDate);
    return daysSinceDue > QUINQUENIO_MAX_DAYS;
  }

  /**
   * Construye el snapshot inmutable del último trimestre indemnizable para la Tabla II del
   * Ministerio de Trabajo.
   *
   * @param monthlyBase Lista de sueldo base de los últimos 3 meses
   * @param monthlyOthers Lista de otros conceptos de los últimos 3 meses
   * @return IndemnizableTrimSnapshot con promedios calculados
   */
  public static IndemnizableTrimSnapshot buildTrimSnapshot(
      List<BigDecimal> monthlyBase, List<BigDecimal> monthlyOthers) {
    return IndemnizableTrimSnapshot.build(monthlyBase, monthlyOthers);
  }
}
