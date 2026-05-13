package com.solveria.core.financial.application.usecase;

import com.solveria.core.financial.application.port.UfvQuotationPort;
import com.solveria.core.financial.domain.event.UfvMaintenanceAppliedEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import com.solveria.core.shared.outbox.domain.DomainRoot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case: Aplicar Mantenimiento de Valor UFV a provisiones y crédito fiscal.
 *
 * <p>Flujo:
 * 1. Recibe fecha inicio y fin del período.
 * 2. Consulta UfvQuotationPort para obtener UFV inicial y final.
 * 3. Calcula factor de actualización: ufvFinal / ufvInicial.
 * 4. Actualiza saldos de provisiones/crédito fiscal multiplicando por el factor.
 * 5. Dispara evento UfvMaintenanceAppliedEvent.
 *
 * <p>Si el proveedor BCB no responde, se propaga UfvProviderUnavailableException.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApplyUfvMaintenanceUseCase extends DomainRoot {

  /** Escala para el factor de actualización UFV (8 decimales de precisión). */
  private static final int UFV_FACTOR_SCALE = 8;

  private final UfvQuotationPort ufvQuotationPort;

  /**
   * Aplica el mantenimiento de valor UFV para el período indicado.
   *
   * @param periodStart Fecha inicio del período
   * @param periodEnd Fecha fin del período
   * @param currentProvisionBalance Saldo actual de provisiones a actualizar
   * @param currentFiscalCredit Crédito fiscal actual a actualizar
   * @param tenantId Identificador del Tenant
   * @return Resultado con los saldos actualizados y el factor aplicado
   */
  @Transactional
  public UfvMaintenanceResult execute(
      LocalDate periodStart,
      LocalDate periodEnd,
      BigDecimal currentProvisionBalance,
      BigDecimal currentFiscalCredit,
      String tenantId) {

    log.info(
        "event=UFV_MAINTENANCE_START periodStart={} periodEnd={} tenantId={}",
        periodStart,
        periodEnd,
        tenantId);

    // --- Paso 2: Obtener cotizaciones UFV ---
    BigDecimal ufvInicial = ufvQuotationPort.getUfvValue(periodStart);
    BigDecimal ufvFinal = ufvQuotationPort.getUfvValue(periodEnd);

    log.info(
        "event=UFV_QUOTATIONS_RETRIEVED ufvInicial={} ufvFinal={}",
        ufvInicial,
        ufvFinal);

    // --- Paso 3: Calcular factor de actualización ---
    if (ufvInicial.compareTo(BigDecimal.ZERO) <= 0) {
      log.warn("event=UFV_INVALID_INITIAL_VALUE ufvInicial={}", ufvInicial);
      throw new IllegalStateException("UFV inicial debe ser mayor a cero");
    }

    BigDecimal adjustmentFactor =
        ufvFinal.divide(ufvInicial, UFV_FACTOR_SCALE, RoundingMode.HALF_UP);

    log.info("event=UFV_FACTOR_CALCULATED factor={}", adjustmentFactor);

    // --- Paso 4: Actualizar saldos ---
    BigDecimal updatedProvisions =
        currentProvisionBalance
            .multiply(adjustmentFactor)
            .setScale(2, RoundingMode.HALF_UP);

    BigDecimal updatedFiscalCredit =
        currentFiscalCredit
            .multiply(adjustmentFactor)
            .setScale(2, RoundingMode.HALF_UP);

    log.info(
        "event=UFV_BALANCES_UPDATED provisions={} fiscalCredit={}",
        updatedProvisions,
        updatedFiscalCredit);

    // --- Paso 5: Disparar evento ---
    registerEvent(
        new UfvMaintenanceAppliedEvent(
            periodStart, periodEnd, ufvInicial, ufvFinal, adjustmentFactor, tenantId));

    log.info(
        "event=UFV_MAINTENANCE_APPLIED tenantId={} factor={}",
        tenantId,
        adjustmentFactor);

    return new UfvMaintenanceResult(
        adjustmentFactor, updatedProvisions, updatedFiscalCredit, ufvInicial, ufvFinal);
  }

  /** Resultado del mantenimiento de valor UFV con saldos actualizados. */
  public record UfvMaintenanceResult(
      BigDecimal adjustmentFactor,
      BigDecimal updatedProvisionBalance,
      BigDecimal updatedFiscalCredit,
      BigDecimal ufvInicial,
      BigDecimal ufvFinal) {}
}
