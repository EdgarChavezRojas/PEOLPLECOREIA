package com.solveria.core.dossier.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record DisciplinaryThresholdReachedEvent(UUID relationshipId, Instant occurredAt)
    implements DomainEvent {

  public DisciplinaryThresholdReachedEvent {
    if (relationshipId == null || occurredAt == null) {
      throw new IllegalArgumentException();
    }
  }

  public static DisciplinaryThresholdReachedEvent now(UUID relationshipId) {
    return new DisciplinaryThresholdReachedEvent(relationshipId, Instant.now());
  }
}
