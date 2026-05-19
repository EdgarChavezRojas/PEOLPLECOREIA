package com.solveria.core.experience.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Evento (Async): Solicitud de cambio de datos cancelada por el empleado solicitante (ESS).
 * Emitido cuando el empleado cancela una solicitud que estaba en estado PENDING_REVIEW.
 */
public record DataChangeCancelledEvent(
    UUID actionId,
    UUID personId,
    UUID tenantId,
    Instant occurredAt)
    implements DomainEvent {

  public DataChangeCancelledEvent(UUID actionId, UUID personId, UUID tenantId) {
    this(actionId, personId, tenantId, Instant.now());
  }
}
