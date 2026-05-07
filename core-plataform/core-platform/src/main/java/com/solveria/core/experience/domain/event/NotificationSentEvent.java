package com.solveria.core.experience.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * Evento (Async): Notificación enviada al destinatario vía canal configurado. Registra auditoría de
 * entrega para cumplimiento legal (ej. notificación de memorandos).
 */
public record NotificationSentEvent(
    UUID notificationId,
    UUID recipientId,
    String channel,
    String subject,
    String tenantId,
    Instant occurredAt)
    implements DomainEvent {

  public NotificationSentEvent(
      UUID notificationId, UUID recipientId, String channel, String subject, String tenantId) {
    this(notificationId, recipientId, channel, subject, tenantId, Instant.now());
  }
}
