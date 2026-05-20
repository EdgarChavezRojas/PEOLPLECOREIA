package com.solveria.core.financial.application.usecase;

import com.solveria.core.financial.domain.event.PrimaCalculatedEvent;
import com.solveria.core.shared.outbox.domain.DomainRoot;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case: Distribución de Utilidades / Primas según tipo de Tenant.
 *
 * <p>Flujo: 1. Recibe tenantId, tipo de tenant, utilidad neta y datos de planilla. 2. Invariante
 * P2: Si Tenant es ONG/Fundación o Educación, retorna 0 (exención total). 3. Si es
 * Retail/Corporativo: calcula límite legal del pozo (25% de utilidad neta). 4. Compara el pozo con
 * el costo de 1 sueldo por cada empleado elegible. 5. Si el 25% no cubre la planilla completa,
 * distribuye el pozo equitativamente (prorrateo). 6. Dispara evento PrimaCalculatedEvent.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EvaluatePrimaAnnualUseCase extends DomainRoot {

  /** Porcentaje legal máximo del pozo de distribución de utilidades (25%). */
  private static final BigDecimal POOL_PERCENTAGE = new BigDecimal("0.25");

  /**
   * Evalúa y calcula la prima anual / distribución de utilidades.
   *
   * @param tenantId Identificador del Tenant
   * @param tenantType Tipo de organización del Tenant
   * @param fiscalYear Año fiscal
   * @param utilidadNeta Utilidad neta del ejercicio fiscal
   * @param eligibleEmployeeCount Cantidad de empleados elegibles
   * @param totalPayrollCost Costo total de 1 sueldo promedio × cada empleado elegible
   * @return Monto individual de prima por empleado (ZERO si exento o sin utilidad)
   */
  @Transactional
  public BigDecimal execute(
      String tenantId,
      TenantType tenantType,
      int fiscalYear,
      BigDecimal utilidadNeta,
      int eligibleEmployeeCount,
      BigDecimal totalPayrollCost) {

    log.info(
        "event=PRIMA_ANNUAL_START tenantId={} tenantType={} fiscalYear={} utilidadNeta={}",
        tenantId,
        tenantType,
        fiscalYear,
        utilidadNeta);

    // --- Invariante P2: Exención total para ONG/Fundación y Educación ---
    if (tenantType == TenantType.ONG_FUNDACION || tenantType == TenantType.EDUCACION) {
      log.info(
          "event=PRIMA_EXEMPT tenantId={} tenantType={} reason=P2_EXENCION_TOTAL",
          tenantId,
          tenantType);

      registerEvent(
          new PrimaCalculatedEvent(
              tenantId, fiscalYear, utilidadNeta, BigDecimal.ZERO, BigDecimal.ZERO, 0, false));

      return BigDecimal.ZERO;
    }

    // --- Validar utilidad positiva ---
    if (utilidadNeta == null || utilidadNeta.compareTo(BigDecimal.ZERO) <= 0) {
      log.info("event=PRIMA_NO_PROFIT tenantId={} utilidadNeta={}", tenantId, utilidadNeta);
      return BigDecimal.ZERO;
    }

    if (eligibleEmployeeCount <= 0) {
      log.info("event=PRIMA_NO_ELIGIBLE_EMPLOYEES tenantId={}", tenantId);
      return BigDecimal.ZERO;
    }

    // --- Paso 3: Calcular pozo legal (25% de utilidad neta) ---
    BigDecimal poolAmount =
        utilidadNeta.multiply(POOL_PERCENTAGE).setScale(2, RoundingMode.HALF_UP);

    log.info(
        "event=PRIMA_POOL_CALCULATED tenantId={} pool={} percentage={}",
        tenantId,
        poolAmount,
        POOL_PERCENTAGE);

    // --- Paso 4-5: Comparar pozo con planilla y distribuir ---
    BigDecimal perEmployeeAmount;
    boolean prorateApplied;

    if (poolAmount.compareTo(totalPayrollCost) >= 0) {
      // El pozo cubre al menos 1 sueldo por empleado
      perEmployeeAmount =
          totalPayrollCost.divide(new BigDecimal(eligibleEmployeeCount), 2, RoundingMode.HALF_UP);
      prorateApplied = false;

      log.info("event=PRIMA_FULL_COVERAGE tenantId={} perEmployee={}", tenantId, perEmployeeAmount);
    } else {
      // El 25% no cubre la planilla → prorrateo equitativo
      perEmployeeAmount =
          poolAmount.divide(new BigDecimal(eligibleEmployeeCount), 2, RoundingMode.HALF_UP);
      prorateApplied = true;

      log.warn(
          "event=PRIMA_PRORATE_APPLIED tenantId={} pool={} payrollCost={} perEmployee={}",
          tenantId,
          poolAmount,
          totalPayrollCost,
          perEmployeeAmount);
    }

    // --- Paso 6: Disparar evento ---
    registerEvent(
        new PrimaCalculatedEvent(
            tenantId,
            fiscalYear,
            utilidadNeta,
            poolAmount,
            perEmployeeAmount,
            eligibleEmployeeCount,
            prorateApplied));

    log.info(
        "event=PRIMA_CALCULATED tenantId={} perEmployee={} eligible={} prorate={}",
        tenantId,
        perEmployeeAmount,
        eligibleEmployeeCount,
        prorateApplied);

    return perEmployeeAmount;
  }

  /** Tipos de Tenant para control de invariante P2. */
  public enum TenantType {
    /** Empresa Retail — sujeta a distribución de utilidades. */
    RETAIL,
    /** Empresa Corporativa — sujeta a distribución de utilidades. */
    CORPORATIVO,
    /** ONG o Fundación — exenta de distribución (P2). */
    ONG_FUNDACION,
    /** Institución Educativa — exenta de distribución (P2). */
    EDUCACION
  }
}
