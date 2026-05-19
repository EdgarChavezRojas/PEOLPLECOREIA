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
    UUID relationshipId,
    String titleLevel,
    boolean provNacional,
    Instant occurredAt
) implements DomainEvent {

  public DocentAcademicTitleVerifiedEvent {
    if (relationshipId == null) {
      throw new IllegalArgumentException();
    }
    if (titleLevel == null || titleLevel.isBlank()) {
      throw new IllegalArgumentException();
    }
    if (occurredAt == null) {
      throw new IllegalArgumentException();
    }
  }

  public static DocentAcademicTitleVerifiedEvent now(
      UUID relationshipId, String titleLevel, boolean provNacional) {
    return new DocentAcademicTitleVerifiedEvent(relationshipId, titleLevel, provNacional, Instant.now());
  }
}
