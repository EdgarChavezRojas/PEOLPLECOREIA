package com.solveria.core.accruals.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record QuinquenioRequestedEvent(
    UUID provisionId,
    UUID relationshipId,
    LocalDate requestDate,
    Instant occurredAt
) implements DomainEvent {

  public QuinquenioRequestedEvent {
    if (provisionId == null) {
      throw new IllegalArgumentException("provisionId es requerido");
    }
    if (relationshipId == null) {
      throw new IllegalArgumentException("relationshipId es requerido");
    }
    if (requestDate == null) {
      throw new IllegalArgumentException("requestDate es requerido");
    }
    if (occurredAt == null) {
      throw new IllegalArgumentException("occurredAt es requerido");
    }
  }

  public static QuinquenioRequestedEvent now(
      UUID provisionId, UUID relationshipId, LocalDate requestDate) {
    return new QuinquenioRequestedEvent(provisionId, relationshipId, requestDate, Instant.now());
  }
}

