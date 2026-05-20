package com.solveria.core.accruals.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Emitido al detectar 60 meses de antigüedad ininterrumpida. Trigger: 60MonthsSeniorityDetected.
 * Habilita el derecho al cobro de la indemnización acumulada (Quinquenio). En Bolivia, el
 * quinquenio es un derecho consolidado a los 5 años.
 */
public record QuinquenioEligibilityReachedEvent(
    UUID personId, LocalDate milestoneDate, Instant occurredAt) implements DomainEvent {

  public QuinquenioEligibilityReachedEvent {
    if (personId == null) {
      throw new IllegalArgumentException("personId es requerido");
    }
    if (milestoneDate == null) {
      throw new IllegalArgumentException("milestoneDate es requerido");
    }
    if (occurredAt == null) {
      throw new IllegalArgumentException("occurredAt es requerido");
    }
  }

  public static QuinquenioEligibilityReachedEvent now(UUID personId, LocalDate milestoneDate) {
    return new QuinquenioEligibilityReachedEvent(personId, milestoneDate, Instant.now());
  }
}
