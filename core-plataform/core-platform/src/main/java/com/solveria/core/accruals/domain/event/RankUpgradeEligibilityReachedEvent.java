package com.solveria.core.accruals.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record RankUpgradeEligibilityReachedEvent(
    UUID relationshipId,
    String currentRank,
    Instant occurredAt
) implements DomainEvent {

  public RankUpgradeEligibilityReachedEvent {
    if (relationshipId == null) {
      throw new IllegalArgumentException("relationshipId es requerido");
    }
    if (currentRank == null || currentRank.isBlank()) {
      throw new IllegalArgumentException("currentRank es requerido");
    }
    if (occurredAt == null) {
      throw new IllegalArgumentException("occurredAt es requerido");
    }
  }

  public static RankUpgradeEligibilityReachedEvent now(UUID relationshipId, String currentRank) {
    return new RankUpgradeEligibilityReachedEvent(relationshipId, currentRank, Instant.now());
  }
}

