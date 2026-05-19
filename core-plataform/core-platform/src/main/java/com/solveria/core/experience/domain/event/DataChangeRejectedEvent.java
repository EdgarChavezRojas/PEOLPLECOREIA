package com.solveria.core.experience.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Evento (Async): Solicitud de cambio de datos rechazada por MSS (W11). Genera notificación push al
 * empleado solicitante.
 */
public record DataChangeRejectedEvent(
    UUID actionId,
    UUID personId,
    String rejectionReason,
    UUID rejectedBy,
    UUID tenantId,
    Instant occurredAt)
    implements DomainEvent {

  public DataChangeRejectedEvent(
      UUID actionId, UUID personId, String rejectionReason, UUID rejectedBy, UUID tenantId) {
    this(actionId, personId, rejectionReason, rejectedBy, tenantId, Instant.now());
  }
}
