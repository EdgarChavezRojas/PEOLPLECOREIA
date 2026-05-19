package com.solveria.core.dossier.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record EligibilityRestoredEvent(UUID relationshipId, Instant occurredAt)
    implements DomainEvent {

  public EligibilityRestoredEvent {
    if (relationshipId == null || occurredAt == null) {
      throw new IllegalArgumentException();
    }
  }

  public static EligibilityRestoredEvent now(UUID relationshipId) {
    return new EligibilityRestoredEvent(relationshipId, Instant.now());
  }
}

