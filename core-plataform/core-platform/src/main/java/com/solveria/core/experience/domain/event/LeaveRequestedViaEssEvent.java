package com.solveria.core.experience.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Evento de integración: Solicitud de ausencia/permiso creada vía ESS.
 * Publicado para que BC de Tiempos y Marcaciones procese la ausencia.
 */
public record LeaveRequestedViaEssEvent(
    UUID actionId,
    UUID personId,
    String leaveType,
    LocalDate startDate,
    LocalDate endDate,
    String tenantId,
    Instant occurredAt)
    implements DomainEvent {

  public LeaveRequestedViaEssEvent(
      UUID actionId,
      UUID personId,
      String leaveType,
      LocalDate startDate,
      LocalDate endDate,
      String tenantId) {
    this(actionId, personId, leaveType, startDate, endDate, tenantId, Instant.now());
  }
}
