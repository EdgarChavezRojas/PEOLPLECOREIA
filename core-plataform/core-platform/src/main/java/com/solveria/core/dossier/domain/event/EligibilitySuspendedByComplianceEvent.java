package com.solveria.core.dossier.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record EligibilitySuspendedByComplianceEvent(
    UUID relationshipId, UUID tenantId, Instant occurredAt) implements DomainEvent {

  public EligibilitySuspendedByComplianceEvent {
    if (relationshipId == null || occurredAt == null) {
      throw new IllegalArgumentException();
    }
  }

  public static EligibilitySuspendedByComplianceEvent now(UUID relationshipId, UUID tenantId) {
    return new EligibilitySuspendedByComplianceEvent(relationshipId, tenantId, Instant.now());
  }
}
