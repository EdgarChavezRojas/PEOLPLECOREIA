package com.solveria.core.accruals.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AccrualBalanceDeductedEvent(
    UUID balanceId,
    BigDecimal deductedDays,
    Instant occurredAt
) implements DomainEvent {

  public AccrualBalanceDeductedEvent {
    if (balanceId == null) {
      throw new IllegalArgumentException("balanceId es requerido");
    }
    if (deductedDays == null || deductedDays.signum() <= 0) {
      throw new IllegalArgumentException("deductedDays debe ser positivo");
    }
    if (occurredAt == null) {
      throw new IllegalArgumentException("occurredAt es requerido");
    }
  }

  public static AccrualBalanceDeductedEvent now(UUID balanceId, BigDecimal deductedDays) {
    return new AccrualBalanceDeductedEvent(balanceId, deductedDays, Instant.now());
  }
}

