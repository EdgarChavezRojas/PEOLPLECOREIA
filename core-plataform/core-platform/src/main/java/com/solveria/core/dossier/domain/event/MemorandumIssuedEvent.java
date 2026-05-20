package com.solveria.core.dossier.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record MemorandumIssuedEvent(UUID relationshipId, Instant occurredAt)
    implements DomainEvent {

  public MemorandumIssuedEvent {
    if (relationshipId == null || occurredAt == null) {
      throw new IllegalArgumentException();
    }
  }

  public static MemorandumIssuedEvent now(UUID relationshipId) {
    return new MemorandumIssuedEvent(relationshipId, Instant.now());
  }
}
