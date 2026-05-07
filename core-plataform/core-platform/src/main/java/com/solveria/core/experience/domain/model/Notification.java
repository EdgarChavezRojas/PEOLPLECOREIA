package com.solveria.core.experience.domain.model;

import com.solveria.core.experience.domain.event.NotificationSentEvent;
import com.solveria.core.experience.domain.model.vo.NotificationChannel;
import com.solveria.core.shared.events.DomainEvent;
import java.time.Instant;
import java.util.*;

public class Notification {
  private UUID notificationId;
  private UUID recipientId;
  private NotificationChannel channel;
  private String subject;
  private String body;
  private String tenantId;
  private Instant sentAt;
  private Instant readAt;
  private final List<DomainEvent> domainEvents = new ArrayList<>();

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
    n.domainEvents.add(
        new NotificationSentEvent(
            n.notificationId, recipientId, channel.name(), subject, tenantId));
    return n;
  }

  public void markAsRead() {
    if (this.readAt != null) return;
    this.readAt = Instant.now();
  }

  public static Notification rehydrate(
      UUID notificationId,
      UUID recipientId,
      NotificationChannel channel,
      String subject,
      String body,
      String tenantId,
      Instant sentAt,
      Instant readAt) {
    Notification n = new Notification();
    n.notificationId = notificationId;
    n.recipientId = recipientId;
    n.channel = channel;
    n.subject = subject;
    n.body = body;
    n.tenantId = tenantId;
    n.sentAt = sentAt;
    n.readAt = readAt;
    return n;
  }

  public List<DomainEvent> pullDomainEvents() {
    List<DomainEvent> events = new ArrayList<>(domainEvents);
    domainEvents.clear();
    return Collections.unmodifiableList(events);
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
}
