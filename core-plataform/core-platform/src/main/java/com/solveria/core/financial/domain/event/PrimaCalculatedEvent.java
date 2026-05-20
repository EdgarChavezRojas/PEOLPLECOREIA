package com.solveria.core.financial.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Evento (Async): Prima Anual / Distribución de Utilidades calculada para un Tenant. Trigger:
 * Cierre fiscal anual con utilidad neta positiva.
 *
 * <p>Invariante P2: Si el Tenant es ONG/Fundación o Educación, el monto será ZERO (exención total).
 * Para Retail/Corporativo, el pozo legal es máximo 25% de la utilidad neta, distribuido
 * equitativamente si no cubre 1 sueldo por empleado.
 */
public record PrimaCalculatedEvent(
    String tenantId,
    int fiscalYear,
    BigDecimal utilidadNeta,
    BigDecimal poolAmount,
    BigDecimal perEmployeeAmount,
    int eligibleCount,
    boolean prorateApplied,
    Instant occurredAt)
    implements DomainEvent {

  public PrimaCalculatedEvent(
      String tenantId,
      int fiscalYear,
      BigDecimal utilidadNeta,
      BigDecimal poolAmount,
      BigDecimal perEmployeeAmount,
      int eligibleCount,
      boolean prorateApplied) {
    this(
        tenantId,
        fiscalYear,
        utilidadNeta,
        poolAmount,
        perEmployeeAmount,
        eligibleCount,
        prorateApplied,
        Instant.now());
  }
}
