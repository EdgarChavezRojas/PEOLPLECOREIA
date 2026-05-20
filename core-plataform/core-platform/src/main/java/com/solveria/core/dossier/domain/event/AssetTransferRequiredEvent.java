package com.solveria.core.dossier.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record AssetTransferRequiredEvent(UUID assignmentId, UUID workerId, Instant occurredAt)
    implements DomainEvent {

  public AssetTransferRequiredEvent {
    if (assignmentId == null || workerId == null || occurredAt == null) {
      throw new IllegalArgumentException();
    }
  }

  public static AssetTransferRequiredEvent now(UUID assignmentId, UUID workerId) {
    return new AssetTransferRequiredEvent(assignmentId, workerId, Instant.now());
  }
}
