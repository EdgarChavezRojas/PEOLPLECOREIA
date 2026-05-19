package com.solveria.core.accruals.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record QuinquenioCalculationFinalizedEvent(
    UUID provisionId,
    UUID relationshipId,
    BigDecimal averageLast90Days,
    Instant occurredAt
) implements DomainEvent {

  public QuinquenioCalculationFinalizedEvent {
    if (provisionId == null) {
      throw new IllegalArgumentException("provisionId es requerido");
    }
    if (relationshipId == null) {
      throw new IllegalArgumentException("relationshipId es requerido");
    }
    if (averageLast90Days == null || averageLast90Days.signum() <= 0) {
      throw new IllegalArgumentException("averageLast90Days debe ser positivo");
    }
    if (occurredAt == null) {
      throw new IllegalArgumentException("occurredAt es requerido");
    }
  }

  public static QuinquenioCalculationFinalizedEvent now(
      UUID provisionId, UUID relationshipId, BigDecimal averageLast90Days) {
    return new QuinquenioCalculationFinalizedEvent(
        provisionId, relationshipId, averageLast90Days, Instant.now());
  }
}

