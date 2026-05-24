package com.solveria.core.financial.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Evento (Sync/Bloqueante): Verifica que ProjectID tenga saldo para sueldo/cargas antes de aprobar
 * contratos.
 */
public record FundingSourceValidatedEvent(
    UUID sourceId, BigDecimal validatedAmount, Instant occurredAt) implements DomainEvent {

  public FundingSourceValidatedEvent(UUID sourceId, BigDecimal validatedAmount) {
    this(sourceId, validatedAmount, Instant.now());
  }
}
