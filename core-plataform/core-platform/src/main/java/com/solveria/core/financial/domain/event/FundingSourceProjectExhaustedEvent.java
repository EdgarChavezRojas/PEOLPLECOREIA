package com.solveria.core.financial.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/** Evento (Async): Alerta cuando la partida presupuestaria llega al 0%. */
public record FundingSourceProjectExhaustedEvent(
    UUID sourceId, String projectCode, Instant occurredAt) implements DomainEvent {

  public FundingSourceProjectExhaustedEvent(UUID sourceId, String projectCode) {
    this(sourceId, projectCode, Instant.now());
  }
}
