package com.solveria.core.experience.application.usecase;

import com.solveria.core.experience.application.command.SendNotificationCommand;
import com.solveria.core.experience.application.port.in.NotificationPI;
import com.solveria.core.experience.application.port.out.NotificationPO;
import com.solveria.core.experience.domain.model.Notification;
import com.solveria.core.experience.domain.model.vo.NotificationChannel;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Use Case: Envío de Notificaciones. Soporta PUSH_MOBILE y EMAIL según spec. */
@Slf4j
@Service
@RequiredArgsConstructor
public class SendNotificationUseCase implements NotificationPI {

  private final NotificationPO notificationPO;

  @Transactional
  public UUID send(UUID recipientId, String channel, String subject, String body, UUID tenantId) {
    log.info(
        "event=NOTIFICATION_SEND recipientId={} channel={} subject={}",
        recipientId,
        channel,
        subject);

    SendNotificationCommand cmd = new SendNotificationCommand(recipientId, channel, subject, body);

    NotificationChannel ch = NotificationChannel.valueOf(cmd.channel());
    Notification notification =
        Notification.send(cmd.recipientId(), ch, cmd.subject(), cmd.body(), tenantId);

    notificationPO.save(notification);

    log.info(
        "event=NOTIFICATION_SENT notifId={} recipientId={}",
        notification.getNotificationId(),
        recipientId);
    return notification.getNotificationId();
  }

  @Transactional
  public void markAsRead(UUID notificationId) {
    Notification notification =
        notificationPO
            .findById(notificationId)
            .orElseThrow(
                () ->
                    new IllegalArgumentException("Notification no encontrada: " + notificationId));
    notification.markAsRead();
    notificationPO.save(notification);
    log.info("event=NOTIFICATION_READ notifId={}", notificationId);
  }
}
