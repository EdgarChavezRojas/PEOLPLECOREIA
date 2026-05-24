package com.solveria.core.dossier.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record DocumentValidationRejectedEvent(
    UUID docId, UUID relationshipId, String reason, Instant occurredAt) implements DomainEvent {

  public DocumentValidationRejectedEvent {
    if (docId == null || relationshipId == null || occurredAt == null) {
      throw new IllegalArgumentException();
    }
    if (reason == null || reason.isBlank()) {
      throw new IllegalArgumentException();
    }
  }

  public static DocumentValidationRejectedEvent now(
      UUID docId, UUID relationshipId, String reason) {
    return new DocumentValidationRejectedEvent(docId, relationshipId, reason, Instant.now());
  }
}
