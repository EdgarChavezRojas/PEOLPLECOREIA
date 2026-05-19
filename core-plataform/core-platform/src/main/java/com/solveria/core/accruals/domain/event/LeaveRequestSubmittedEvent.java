package com.solveria.core.accruals.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record LeaveRequestSubmittedEvent(
    UUID balanceId,
    UUID transactionId,
    LocalDate startDate,
    LocalDate endDate,
    BigDecimal chargeableDays,
    Instant occurredAt
) implements DomainEvent {

  public LeaveRequestSubmittedEvent {
    if (balanceId == null) {
      throw new IllegalArgumentException("balanceId es requerido");
    }
    if (transactionId == null) {
      throw new IllegalArgumentException("transactionId es requerido");
    }
    if (startDate == null) {
      throw new IllegalArgumentException("startDate es requerido");
    }
    if (endDate == null) {
      throw new IllegalArgumentException("endDate es requerido");
    }
    if (chargeableDays == null || chargeableDays.signum() <= 0) {
      throw new IllegalArgumentException("chargeableDays debe ser positivo");
    }
    if (occurredAt == null) {
      throw new IllegalArgumentException("occurredAt es requerido");
    }
  }

  public static LeaveRequestSubmittedEvent now(
      UUID balanceId,
      UUID transactionId,
      LocalDate startDate,
      LocalDate endDate,
      BigDecimal chargeableDays) {
    return new LeaveRequestSubmittedEvent(
        balanceId, transactionId, startDate, endDate, chargeableDays, Instant.now());
  }
}

