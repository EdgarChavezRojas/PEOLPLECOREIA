package com.solveria.core.experience.domain.model;

import com.solveria.core.experience.domain.event.MemorandumAcknowledgedEvent;
import com.solveria.core.experience.domain.event.NotificationSentEvent;
import com.solveria.core.experience.domain.model.vo.NotificationChannel;
import com.solveria.core.shared.outbox.domain.DomainRoot;

import java.time.Instant;
import java.util.*;

public class Notification extends DomainRoot {
  private UUID notificationId;
  private UUID recipientId;
  private NotificationChannel channel;
  private String subject;
  private String body;
  private String tenantId;
  private Instant sentAt;
  private Instant readAt;

  /** Indica si esta notificación es un memorando que requiere acuse de recibo formal. */
  private boolean requiresAcknowledgement;
  /** Timestamp del acuse de recibo (firma). */
  private Instant acknowledgedAt;
  /** ID de la persona que firmó el acuse. */
  private UUID acknowledgedBy;


  private Notification() {}

  public static Notification send(
      UUID recipientId, NotificationChannel channel, String subject, String body, String tenantId) {
    if (recipientId == null) throw new IllegalArgumentException("recipientId no puede ser nulo");
    if (channel == null) throw new IllegalArgumentException("Canal no puede ser nulo");
    if (subject == null || subject.isBlank()) throw new IllegalArgumentException("Asunto vacío");

    Notification n = new Notification();
    n.notificationId = UUID.randomUUID();
    n.recipientId = recipientId;
    n.channel = channel;
    n.subject = subject;
    n.body = body;
    n.tenantId = tenantId;
    n.sentAt = Instant.now();
    n.readAt = null;
    n.requiresAcknowledgement = false;
    n.acknowledgedAt = null;
    n.acknowledgedBy = null;
    n.registerEvent(
        new NotificationSentEvent(
            n.notificationId, recipientId, channel.name(), subject, tenantId));
    return n;
  }

  /**
   * Crea una notificación de tipo memorando que requiere acuse de recibo formal (W12).
   *
   * @param recipientId ID del destinatario
   * @param subject     Asunto del memorando
   * @param body        Contenido del memorando
   * @param tenantId    Tenant
   */
  public static Notification sendMemorandum(
      UUID recipientId, String subject, String body, String tenantId) {
    if (recipientId == null) throw new IllegalArgumentException("recipientId no puede ser nulo");
    if (subject == null || subject.isBlank()) throw new IllegalArgumentException("Asunto vacío");

    Notification n = new Notification();
    n.notificationId = UUID.randomUUID();
    n.recipientId = recipientId;
    n.channel = NotificationChannel.EMAIL;
    n.subject = subject;
    n.body = body;
    n.tenantId = tenantId;
    n.sentAt = Instant.now();
    n.readAt = null;
    n.requiresAcknowledgement = true;
    n.acknowledgedAt = null;
    n.acknowledgedBy = null;
    n.registerEvent(
        new NotificationSentEvent(
            n.notificationId, recipientId, NotificationChannel.EMAIL.name(), subject, tenantId));
    return n;
  }

  public void markAsRead() {
    if (this.readAt != null) return;
    this.readAt = Instant.now();
  }

  /**
   * W12: Acuse de recibo formal del memorando. Solo memorandos que requieren
   * acknowledgement pueden ser firmados, y solo por el destinatario.
   *
   * @param personId ID de la persona que firma el acuse
   */
  public void acknowledge(UUID personId) {
    if (!this.requiresAcknowledgement) {
      throw new IllegalStateException(
          "Esta notificación no requiere acuse de recibo");
    }
    if (this.acknowledgedAt != null) {
      throw new IllegalStateException(
          "Este memorando ya fue acusado de recibo el " + this.acknowledgedAt);
    }
    if (!this.recipientId.equals(personId)) {
      throw new IllegalStateException(
          "Solo el destinatario puede firmar el acuse de recibo");
    }
    this.acknowledgedAt = Instant.now();
    this.acknowledgedBy = personId;
    // Marcar como leído implícitamente al firmar
    if (this.readAt == null) {
      this.readAt = this.acknowledgedAt;
    }
    this.registerEvent(
        new MemorandumAcknowledgedEvent(
            this.notificationId, personId, this.acknowledgedAt, this.tenantId));
  }

  public static Notification rehydrate(
      UUID notificationId,
      UUID recipientId,
      NotificationChannel channel,
      String subject,
      String body,
      String tenantId,
      Instant sentAt,
      Instant readAt,
      boolean requiresAcknowledgement,
      Instant acknowledgedAt,
      UUID acknowledgedBy) {
    Notification n = new Notification();
    n.notificationId = notificationId;
    n.recipientId = recipientId;
    n.channel = channel;
    n.subject = subject;
    n.body = body;
    n.tenantId = tenantId;
    n.sentAt = sentAt;
    n.readAt = readAt;
    n.requiresAcknowledgement = requiresAcknowledgement;
    n.acknowledgedAt = acknowledgedAt;
    n.acknowledgedBy = acknowledgedBy;
    return n;
  }



  public UUID getNotificationId() {
    return notificationId;
  }

  public UUID getRecipientId() {
    return recipientId;
  }

  public NotificationChannel getChannel() {
    return channel;
  }

  public String getSubject() {
    return subject;
  }

  public String getBody() {
    return body;
  }

  public String getTenantId() {
    return tenantId;
  }

  public Instant getSentAt() {
    return sentAt;
  }

  public Instant getReadAt() {
    return readAt;
  }

  public boolean isRead() {
    return readAt != null;
  }

  public boolean isRequiresAcknowledgement() {
    return requiresAcknowledgement;
  }

  public Instant getAcknowledgedAt() {
    return acknowledgedAt;
  }

  public UUID getAcknowledgedBy() {
    return acknowledgedBy;
  }

  public boolean isAcknowledged() {
    return acknowledgedAt != null;
  }
}
