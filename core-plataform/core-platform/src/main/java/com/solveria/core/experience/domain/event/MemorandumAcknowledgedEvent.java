package com.solveria.core.experience.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Evento (Async): Memorando acusado de recibo por el empleado (W12).
 * Registra la firma/acuse con timestamp para auditoría y cumplimiento legal.
 */
public record MemorandumAcknowledgedEvent(
    UUID notificationId,
    UUID personId,
    Instant acknowledgedAt,
    String tenantId,
    Instant occurredAt)
    implements DomainEvent {

  public MemorandumAcknowledgedEvent(
      UUID notificationId, UUID personId, Instant acknowledgedAt, String tenantId) {
    this(notificationId, personId, acknowledgedAt, tenantId, Instant.now());
  }
}
