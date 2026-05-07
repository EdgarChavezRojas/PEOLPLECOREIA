package com.solveria.core.dossier.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Emitido al validar un título académico docente contra Provisión Nacional.
 * Trigger: EducationPolicyValidation.
 * Habilita posibles ascensos de categoría docente en el motor de Escalafón.
 */
public record DocentAcademicTitleVerifiedEvent(
    UUID personId,
    String titleLevel,
    boolean provNacional,
    Instant occurredAt
) implements DomainEvent {

  public DocentAcademicTitleVerifiedEvent {
    if (personId == null) {
      throw new IllegalArgumentException("personId es requerido");
    }
    if (titleLevel == null || titleLevel.isBlank()) {
      throw new IllegalArgumentException("titleLevel es requerido");
    }
    if (occurredAt == null) {
      throw new IllegalArgumentException("occurredAt es requerido");
    }
  }

  public static DocentAcademicTitleVerifiedEvent now(
      UUID personId, String titleLevel, boolean provNacional) {
    return new DocentAcademicTitleVerifiedEvent(personId, titleLevel, provNacional, Instant.now());
  }
}
