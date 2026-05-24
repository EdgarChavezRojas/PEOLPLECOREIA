package com.solveria.core.dossier.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record AssetReturnedEvent(UUID assignmentId, UUID workerId, Instant occurredAt)
    implements DomainEvent {

  public AssetReturnedEvent {
    if (assignmentId == null || workerId == null || occurredAt == null) {
      throw new IllegalArgumentException();
    }
  }

  public static AssetReturnedEvent now(UUID assignmentId, UUID workerId) {
    return new AssetReturnedEvent(assignmentId, workerId, Instant.now());
  }
}
