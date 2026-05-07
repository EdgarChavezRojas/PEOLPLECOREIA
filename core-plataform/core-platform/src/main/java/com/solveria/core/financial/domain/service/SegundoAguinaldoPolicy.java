package com.solveria.core.financial.domain.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Política de Dominio P19: Segundo Aguinaldo ("Esfuerzo por Bolivia"). Decreto Supremo 1802
 * (Bolivia).
 *
 * <p>Condicional al crecimiento del PIB anual > 4.5%. Modelado como Feature Toggle: el flag
 * `gdpGrowthToggleActive` es controlado por configuración externa (resolución gubernamental anual).
 *
 * <p>Cálculo: equivalente a 1 salario promedio completo (igual al Aguinaldo navideño).
 *
 * <p>Clase pura sin anotaciones de infraestructura.
 */
public final class SegundoAguinaldoPolicy {

  /** Umbral mínimo de crecimiento del PIB para activar el Segundo Aguinaldo (4.5%). */
  public static final BigDecimal GDP_GROWTH_THRESHOLD = new BigDecimal("4.5");

  private SegundoAguinaldoPolicy() {
    // Utility class
  }

  /**
   * Determina si el Segundo Aguinaldo está activo para el año fiscal.
   *
   * @param gdpGrowthToggleActive Flag de configuración: true si el gobierno decretó el pago
   * @return true si el beneficio aplica
   */
  public static boolean isEligible(boolean gdpGrowthToggleActive) {
    return gdpGrowthToggleActive;
  }

  /**
   * Calcula el monto del Segundo Aguinaldo. Equivalente a 1 salario promedio mensual.
   *
   * @param averageSalary Salario promedio de los últimos 3 meses (P15)
   * @return Monto del Segundo Aguinaldo (o ZERO si no aplica)
   */
  public static BigDecimal calculate(BigDecimal averageSalary, boolean gdpGrowthToggleActive) {
    if (!isEligible(gdpGrowthToggleActive)) {
      return BigDecimal.ZERO;
    }
    if (averageSalary == null || averageSalary.compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO;
    }
    return averageSalary.setScale(2, RoundingMode.HALF_UP);
  }

  /**
   * Calcula el Segundo Aguinaldo proporcional (para trabajadores que no completaron el año).
   *
   * @param averageSalary Salario promedio
   * @param monthsWorked Meses trabajados en el año fiscal
   * @param gdpGrowthToggleActive Flag de configuración
   * @return Monto proporcional del Segundo Aguinaldo
   */
  public static BigDecimal calculateProporcional(
      BigDecimal averageSalary, int monthsWorked, boolean gdpGrowthToggleActive) {
    if (!isEligible(gdpGrowthToggleActive)) {
      return BigDecimal.ZERO;
    }
    if (averageSalary == null || averageSalary.compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO;
    }
    if (monthsWorked <= 0 || monthsWorked > 12) {
      return BigDecimal.ZERO;
    }
    return averageSalary
        .multiply(new BigDecimal(monthsWorked))
        .divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP);
  }
}
