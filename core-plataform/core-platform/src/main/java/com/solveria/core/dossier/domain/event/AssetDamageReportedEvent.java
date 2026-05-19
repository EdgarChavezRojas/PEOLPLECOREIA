package com.solveria.core.dossier.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record AssetDamageReportedEvent(UUID assignmentId, UUID workerId, Instant occurredAt)
    implements DomainEvent {

  public AssetDamageReportedEvent {
    if (assignmentId == null || workerId == null || occurredAt == null) {
      throw new IllegalArgumentException();
    }
  }

  public static AssetDamageReportedEvent now(UUID assignmentId, UUID workerId) {
    return new AssetDamageReportedEvent(assignmentId, workerId, Instant.now());
  }
}

