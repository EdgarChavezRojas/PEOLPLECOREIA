package com.solveria.core.experience.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Evento (Async): Solicitud de cambio de datos personales creada vía ESS (W11). Requiere aprobación
 * MSS (Invariante SoD). Ejemplo: Empleado solicita actualizar dirección o datos bancarios.
 */
public record DataChangeRequestedEvent(
    UUID actionId,
    UUID personId,
    String actionType,
    String payload,
    UUID tenantId,
    Instant occurredAt)
    implements DomainEvent {

  public DataChangeRequestedEvent(
      UUID actionId, UUID personId, String actionType, String payload, UUID tenantId) {
    this(actionId, personId, actionType, payload, tenantId, Instant.now());
  }
}
