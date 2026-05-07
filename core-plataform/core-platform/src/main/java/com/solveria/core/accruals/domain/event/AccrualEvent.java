package com.solveria.core.accruals.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.time.Instant;

public record AccrualEvent(AccrualEventType type, Instant occurredAt) implements DomainEvent {

  public AccrualEvent {
    if (type == null) {
      throw new IllegalArgumentException("type is required");
    }
    if (occurredAt == null) {
      throw new IllegalArgumentException("occurredAt is required");
    }
  }

  public static AccrualEvent now(AccrualEventType type) {
    return new AccrualEvent(type, Instant.now());
  }
}
