package com.solveria.core.dossier.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record HealthCardExpirationWarningEvent(UUID docId, UUID relationshipId, Instant occurredAt)
    implements DomainEvent {

  public HealthCardExpirationWarningEvent {
    if (docId == null || relationshipId == null || occurredAt == null) {
      throw new IllegalArgumentException();
    }
  }

  public static HealthCardExpirationWarningEvent now(UUID docId, UUID relationshipId) {
    return new HealthCardExpirationWarningEvent(docId, relationshipId, Instant.now());
  }
}
