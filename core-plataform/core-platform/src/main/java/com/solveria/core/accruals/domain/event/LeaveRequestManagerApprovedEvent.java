package com.solveria.core.accruals.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LeaveRequestManagerApprovedEvent(
    UUID balanceId,
    UUID transactionId,
    BigDecimal daysRequested,
    Instant occurredAt
) implements DomainEvent {

  public LeaveRequestManagerApprovedEvent {
    if (balanceId == null) {
      throw new IllegalArgumentException("balanceId es requerido");
    }
    if (transactionId == null) {
      throw new IllegalArgumentException("transactionId es requerido");
    }
    if (daysRequested == null || daysRequested.signum() <= 0) {
      throw new IllegalArgumentException("daysRequested debe ser positivo");
    }
    if (occurredAt == null) {
      throw new IllegalArgumentException("occurredAt es requerido");
    }
  }

  public static LeaveRequestManagerApprovedEvent now(
      UUID balanceId, UUID transactionId, BigDecimal daysRequested) {
    return new LeaveRequestManagerApprovedEvent(
        balanceId, transactionId, daysRequested, Instant.now());
  }
}

