package com.solveria.core.dossier.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record MandatoryComplianceDocMissingEvent(UUID relationshipId, Instant occurredAt)
    implements DomainEvent {

  public MandatoryComplianceDocMissingEvent {
    if (relationshipId == null || occurredAt == null) {
      throw new IllegalArgumentException();
    }
  }

  public static MandatoryComplianceDocMissingEvent now(UUID relationshipId) {
    return new MandatoryComplianceDocMissingEvent(relationshipId, Instant.now());
  }
}
