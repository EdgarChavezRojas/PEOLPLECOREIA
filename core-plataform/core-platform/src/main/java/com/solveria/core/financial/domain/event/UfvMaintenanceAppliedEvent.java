package com.solveria.core.financial.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Evento (Async): Mantenimiento de Valor UFV aplicado a saldos de provisiones y crédito fiscal.
 * Trigger: Cierre de período contable mensual.
 *
 * <p>El factor de actualización se calcula como ufvFinal / ufvInicial del período.
 */
public record UfvMaintenanceAppliedEvent(
    LocalDate periodStart,
    LocalDate periodEnd,
    BigDecimal ufvInicial,
    BigDecimal ufvFinal,
    BigDecimal adjustmentFactor,
    String tenantId,
    Instant occurredAt)
    implements DomainEvent {

  public UfvMaintenanceAppliedEvent(
      LocalDate periodStart,
      LocalDate periodEnd,
      BigDecimal ufvInicial,
      BigDecimal ufvFinal,
      BigDecimal adjustmentFactor,
      String tenantId) {
    this(periodStart, periodEnd, ufvInicial, ufvFinal, adjustmentFactor, tenantId, Instant.now());
  }
}
