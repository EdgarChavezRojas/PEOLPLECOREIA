package com.solveria.core.accruals.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record VacationBalanceThresholdLowEvent(
    UUID balanceId, BigDecimal requestedDays, BigDecimal currentBalance, Instant occurredAt)
    implements DomainEvent {

  public VacationBalanceThresholdLowEvent {
    if (balanceId == null) {
      throw new IllegalArgumentException("balanceId es requerido");
    }
    if (requestedDays == null || requestedDays.signum() <= 0) {
      throw new IllegalArgumentException("requestedDays debe ser positivo");
    }
    if (currentBalance == null || currentBalance.signum() < 0) {
      throw new IllegalArgumentException("currentBalance no puede ser negativo");
    }
    if (occurredAt == null) {
      throw new IllegalArgumentException("occurredAt es requerido");
    }
  }

  public static VacationBalanceThresholdLowEvent now(
      UUID balanceId, BigDecimal requestedDays, BigDecimal currentBalance) {
    return new VacationBalanceThresholdLowEvent(
        balanceId, requestedDays, currentBalance, Instant.now());
  }
}
