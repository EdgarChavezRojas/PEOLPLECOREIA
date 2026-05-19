package com.solveria.core.dossier.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record AssetLoanedToWorkerEvent(UUID assignmentId, UUID workerId, Instant occurredAt)
    implements DomainEvent {

  public AssetLoanedToWorkerEvent {
    if (assignmentId == null || workerId == null || occurredAt == null) {
      throw new IllegalArgumentException();
    }
  }

  public static AssetLoanedToWorkerEvent now(UUID assignmentId, UUID workerId) {
    return new AssetLoanedToWorkerEvent(assignmentId, workerId, Instant.now());
  }
}

