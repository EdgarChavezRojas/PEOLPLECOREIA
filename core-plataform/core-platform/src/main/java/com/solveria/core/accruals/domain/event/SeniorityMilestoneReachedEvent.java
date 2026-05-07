package com.solveria.core.accruals.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Emitido al alcanzar un hito de antigüedad durante el tick mensual de accrual.
 * Trigger: MonthlyAccrualTick.
 * expectedSmMultiplier indica el multiplicador de Salario Mínimo esperado
 * para el cálculo del Bono de Antigüedad.
 */
public record SeniorityMilestoneReachedEvent(
    UUID personId,
    int newSeniorityYears,
    int expectedSmMultiplier,
    Instant occurredAt
) implements DomainEvent {

  public SeniorityMilestoneReachedEvent {
    if (personId == null) {
      throw new IllegalArgumentException("personId es requerido");
    }
    if (newSeniorityYears <= 0) {
      throw new IllegalArgumentException("newSeniorityYears debe ser positivo");
    }
    if (expectedSmMultiplier <= 0) {
      throw new IllegalArgumentException("expectedSmMultiplier debe ser positivo");
    }
    if (occurredAt == null) {
      throw new IllegalArgumentException("occurredAt es requerido");
    }
  }

  public static SeniorityMilestoneReachedEvent now(
      UUID personId, int newSeniorityYears, int expectedSmMultiplier) {
    return new SeniorityMilestoneReachedEvent(
        personId, newSeniorityYears, expectedSmMultiplier, Instant.now());
  }
}
