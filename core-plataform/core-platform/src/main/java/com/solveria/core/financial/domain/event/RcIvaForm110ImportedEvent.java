package com.solveria.core.financial.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Evento (Async): Importa facturas (SIAT) e impacta Sueldo Neto en la planilla. Trigger:
 * TaxFormValidated.
 */
public record RcIvaForm110ImportedEvent(
    UUID formId,
    UUID personId,
    String taxPeriod,
    BigDecimal declaredAmount,
    BigDecimal verifiedCredit,
    Instant occurredAt)
    implements DomainEvent {

  public RcIvaForm110ImportedEvent(
      UUID formId,
      UUID personId,
      String taxPeriod,
      BigDecimal declaredAmount,
      BigDecimal verifiedCredit) {
    this(formId, personId, taxPeriod, declaredAmount, verifiedCredit, Instant.now());
  }
}
