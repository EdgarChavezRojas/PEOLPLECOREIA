package com.solveria.core.workforce.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record PersonUpdatedEvent(UUID personId, UUID tenantId, Instant occurredAt)
    implements DomainEvent {

  public PersonUpdatedEvent(UUID personId, UUID tenantId) {
    this(personId, tenantId, Instant.now());
  }
}
