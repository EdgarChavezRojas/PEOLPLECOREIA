package com.solveria.core.accruals.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Emitido cuando el saldo de un beneficio acumulable se actualiza. Trigger: VacationAccrued.
 * Refleja el nuevo balance tras el devengamiento de vacaciones u otros beneficios.
 */
public record AccrualBalanceUpdatedEvent(
    UUID personId, String benefitType, BigDecimal newBalance, Instant occurredAt)
    implements DomainEvent {

  public AccrualBalanceUpdatedEvent {
    if (personId == null) {
      throw new IllegalArgumentException("personId es requerido");
    }
    if (benefitType == null || benefitType.isBlank()) {
      throw new IllegalArgumentException("benefitType es requerido");
    }
    if (newBalance == null) {
      throw new IllegalArgumentException("newBalance es requerido");
    }
    if (occurredAt == null) {
      throw new IllegalArgumentException("occurredAt es requerido");
    }
  }

  public static AccrualBalanceUpdatedEvent now(
      UUID personId, String benefitType, BigDecimal newBalance) {
    return new AccrualBalanceUpdatedEvent(personId, benefitType, newBalance, Instant.now());
  }
}
