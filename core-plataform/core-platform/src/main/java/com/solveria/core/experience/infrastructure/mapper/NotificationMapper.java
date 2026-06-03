package com.solveria.core.experience.infrastructure.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solveria.core.experience.domain.event.NotificationSentEvent;
import com.solveria.core.experience.domain.model.Notification;
import com.solveria.core.experience.infrastructure.jpa.NotificationJpa;
import com.solveria.core.shared.events.DomainEvent;
import java.util.Map;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface NotificationMapper {

  default NotificationJpa toJpa(Notification notification) {
    if (notification == null) return null;
    NotificationJpa jpa = new NotificationJpa();
    jpa.setNotificationId(notification.getNotificationId());
    jpa.setRecipientId(notification.getRecipientId());
    jpa.setChannel(notification.getChannel());
    jpa.setSubject(notification.getSubject());
    jpa.setBody(notification.getBody());
    jpa.setTenantId(notification.getTenantId());
    jpa.setSentAt(notification.getSentAt());
    jpa.setReadAt(notification.getReadAt());
    jpa.setRequiresAcknowledgement(notification.isRequiresAcknowledgement());
    jpa.setAcknowledgedAt(notification.getAcknowledgedAt());
    jpa.setAcknowledgedBy(notification.getAcknowledgedBy());
    return jpa;
  }

  default void updateJpa(NotificationJpa jpa, Notification notification) {
    if (notification == null || jpa == null) return;
    jpa.setRecipientId(notification.getRecipientId());
    jpa.setChannel(notification.getChannel());
    jpa.setSubject(notification.getSubject());
    jpa.setBody(notification.getBody());
    jpa.setTenantId(notification.getTenantId());
    jpa.setSentAt(notification.getSentAt());
    jpa.setReadAt(notification.getReadAt());
    jpa.setRequiresAcknowledgement(notification.isRequiresAcknowledgement());
    jpa.setAcknowledgedAt(notification.getAcknowledgedAt());
    jpa.setAcknowledgedBy(notification.getAcknowledgedBy());
  }

  default Notification toDomain(NotificationJpa jpa) {
    if (jpa == null) return null;
    return Notification.rehydrate(
        jpa.getNotificationId(),
        jpa.getRecipientId(),
        jpa.getChannel(),
        jpa.getSubject(),
        jpa.getBody(),
        jpa.getTenantId(),
        jpa.getSentAt(),
        jpa.getReadAt(),
        jpa.isRequiresAcknowledgement(), // O jpa.getRequiresAcknowledgement() dependiendo de cómo
        // Lombok generó el getter
        jpa.getAcknowledgedAt(),
        jpa.getAcknowledgedBy());
  }

  default String toEventPayload(Notification notification, DomainEvent event) {
    if (notification == null || event == null) return "{}";
    Map<String, Object> payload =
        Map.of(
            "notificationId", notification.getNotificationId(),
            "recipientId", notification.getRecipientId(),
            "channel", notification.getChannel().name(),
            "tenantId", notification.getTenantId(),
            "eventType", resolveEventType(event));
    try {
      return new ObjectMapper().writeValueAsString(payload);
    } catch (Exception e) {
      throw new RuntimeException("Error serializando Notification a JSON", e);
    }
  }

  default String resolveEventType(DomainEvent event) {
    if (event instanceof NotificationSentEvent) return "NOTIFICATION_SENT";
    return event.getClass().getSimpleName();
  }
}
