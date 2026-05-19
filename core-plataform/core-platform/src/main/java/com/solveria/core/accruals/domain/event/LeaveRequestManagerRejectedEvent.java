package com.solveria.core.accruals.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record LeaveRequestManagerRejectedEvent(
    UUID balanceId,
    UUID transactionId,
    Instant occurredAt
) implements DomainEvent {

  public LeaveRequestManagerRejectedEvent {
    if (balanceId == null) {
      throw new IllegalArgumentException("balanceId es requerido");
    }
    if (transactionId == null) {
      throw new IllegalArgumentException("transactionId es requerido");
    }
    if (occurredAt == null) {
      throw new IllegalArgumentException("occurredAt es requerido");
    }
  }

  public static LeaveRequestManagerRejectedEvent now(UUID balanceId, UUID transactionId) {
    return new LeaveRequestManagerRejectedEvent(balanceId, transactionId, Instant.now());
  }
}

