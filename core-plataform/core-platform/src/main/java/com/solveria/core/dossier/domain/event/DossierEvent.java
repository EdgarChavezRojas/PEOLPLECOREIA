package com.solveria.core.dossier.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.time.Instant;

public record DossierEvent(DossierEventType type, Instant occurredAt) implements DomainEvent {

  public DossierEvent {
    if (type == null) {
      throw new IllegalArgumentException("type es requerido");
    }
    if (occurredAt == null) {
      throw new IllegalArgumentException("occurredAt es requerido");
    }
  }

  public static DossierEvent now(DossierEventType type) {
    return new DossierEvent(type, Instant.now());
  }
}
