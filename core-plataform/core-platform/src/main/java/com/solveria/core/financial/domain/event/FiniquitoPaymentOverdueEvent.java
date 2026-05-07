package com.solveria.core.financial.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Evento (Async): Cronómetro T+16 días activa Multa del 30% sobre el saldo pagable. Se dispara
 * automáticamente si el finiquito no ha sido pagado en 15 días calendario.
 */
public record FiniquitoPaymentOverdueEvent(
    UUID relationshipId,
    UUID personId,
    BigDecimal totalPendiente,
    BigDecimal multaAmount,
    Instant occurredAt)
    implements DomainEvent {

  public FiniquitoPaymentOverdueEvent(
      UUID relationshipId, UUID personId, BigDecimal totalPendiente, BigDecimal multaAmount) {
    this(relationshipId, personId, totalPendiente, multaAmount, Instant.now());
  }
}
