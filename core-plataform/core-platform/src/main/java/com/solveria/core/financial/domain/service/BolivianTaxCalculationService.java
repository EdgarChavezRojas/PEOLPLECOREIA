package com.solveria.core.financial.domain.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Servicio de Dominio: Cálculos tributarios bolivianos (Santa Cruz 2026).
 *
 * <p>Reglas de localización: - Gestora: TG * 12.71% - INFOCAL SCZ: TG * 1% (solo
 * Retail/Corporativo) - RC-IVA: [SN - (2 * SMN)] * 13% - (1 * SMN) * 13% - Form110.verifiedCredit -
 * Desahucio: 3 * SalariosPromedio (si Despido sin Causa) - Multa Finiquito: TotalLíquido * 30% (si
 * > 15 días calendario) - Multa Quinquenio: Monto * 30% (si > 30 días calendario)
 *
 * <p>Clase pura sin anotaciones de infraestructura.
 */
public final class BolivianTaxCalculationService {

  /** Salario Mínimo Nacional vigente 2026. */
  public static final BigDecimal SMN = new BigDecimal("3300.00");

  /** Tasa IVA Transparente (RC-IVA). */
  private static final BigDecimal IVA_RATE = new BigDecimal("0.13");

  /** Tasa de deducción Gestora Pública. */
  private static final BigDecimal GESTORA_RATE = new BigDecimal("0.1271");

  /** Tasa INFOCAL Santa Cruz (comercial/privado). */
  private static final BigDecimal INFOCAL_SCZ_RATE = new BigDecimal("0.01");

  /** Factor de Desahucio: 3 salarios promedio. */
  private static final BigDecimal DESAHUCIO_FACTOR = new BigDecimal("3");

  /** Tasa de Multa por mora (30%). */
  private static final BigDecimal MULTA_RATE = new BigDecimal("0.30");

  private BolivianTaxCalculationService() {
    // Utility class
  }

  /** Calcula la deducción de la Gestora Pública: TG * 12.71%. */
  public static BigDecimal calculateGestoraDeduction(BigDecimal totalGanado) {
    if (totalGanado == null || totalGanado.compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO;
    }
    return totalGanado.multiply(GESTORA_RATE).setScale(2, RoundingMode.HALF_UP);
  }

  /**
   * Calcula el aporte INFOCAL Santa Cruz: TG * 1%. Solo aplica para tenants Retail y Corporativo.
   * Desactivado en ONGs.
   */
  public static BigDecimal calculateInfocalScz(BigDecimal totalGanado) {
    if (totalGanado == null || totalGanado.compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO;
    }
    return totalGanado.multiply(INFOCAL_SCZ_RATE).setScale(2, RoundingMode.HALF_UP);
  }

  /**
   * Calcula el RC-IVA (IVA Transparente 13%). Fórmula: [SN - (2 * SMN)] * 13% - (1 * SMN) * 13% -
   * Form110.verifiedCredit
   *
   * @param sueldoNeto Sueldo neto del trabajador
   * @param form110VerifiedCredit Crédito fiscal verificado del Form 110 (puede ser ZERO)
   * @return Monto RC-IVA a retener (mínimo 0)
   */
  public static BigDecimal calculateRcIva(BigDecimal sueldoNeto, BigDecimal form110VerifiedCredit) {
    if (sueldoNeto == null || sueldoNeto.compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO;
    }
    BigDecimal credit = form110VerifiedCredit != null ? form110VerifiedCredit : BigDecimal.ZERO;

    // Base imponible: SN - (2 * SMN)
    BigDecimal baseImponible = sueldoNeto.subtract(SMN.multiply(new BigDecimal("2")));
    if (baseImponible.compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO;
    }

    // Impuesto bruto: baseImponible * 13%
    BigDecimal impuestoBruto = baseImponible.multiply(IVA_RATE).setScale(2, RoundingMode.HALF_UP);

    // Deducción mínima: (1 * SMN) * 13%
    BigDecimal deduccionMinima = SMN.multiply(IVA_RATE).setScale(2, RoundingMode.HALF_UP);

    // RC-IVA = impuestoBruto - deducciónMínima - créditoForm110
    BigDecimal rcIva = impuestoBruto.subtract(deduccionMinima).subtract(credit);

    // No puede ser negativo
    return rcIva.compareTo(BigDecimal.ZERO) < 0
        ? BigDecimal.ZERO
        : rcIva.setScale(2, RoundingMode.HALF_UP);
  }

  /** Calcula el Desahucio: 3 * Salario Promedio. Solo aplica en caso de Despido sin Causa. */
  public static BigDecimal calculateDesahucio(BigDecimal averageSalary) {
    if (averageSalary == null || averageSalary.compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO;
    }
    return averageSalary.multiply(DESAHUCIO_FACTOR).setScale(2, RoundingMode.HALF_UP);
  }

  /**
   * Calcula la Multa por mora en Finiquito: TotalLíquido * 30%. Aplica si el pago excede 15 días
   * calendario desde la desvinculación.
   */
  public static BigDecimal calculateMultaFiniquito(BigDecimal totalLiquido) {
    if (totalLiquido == null || totalLiquido.compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO;
    }
    return totalLiquido.multiply(MULTA_RATE).setScale(2, RoundingMode.HALF_UP);
  }

  /**
   * Calcula la Multa por mora en Quinquenio: Monto * 30%. Aplica si el pago excede 30 días
   * calendario.
   */
  public static BigDecimal calculateMultaQuinquenio(BigDecimal amount) {
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO;
    }
    return amount.multiply(MULTA_RATE).setScale(2, RoundingMode.HALF_UP);
  }
}
