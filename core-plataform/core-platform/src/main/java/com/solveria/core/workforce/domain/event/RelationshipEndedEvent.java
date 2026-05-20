package com.solveria.core.workforce.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record RelationshipEndedEvent(UUID relationshipId, UUID tenantId, Instant occurredAt)
    implements DomainEvent {

  public RelationshipEndedEvent(UUID relationshipId, UUID tenantId) {
    this(relationshipId, tenantId, Instant.now());
  }
}
