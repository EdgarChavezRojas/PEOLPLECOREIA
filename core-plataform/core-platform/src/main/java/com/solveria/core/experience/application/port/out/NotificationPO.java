package com.solveria.core.experience.application.port.out;

import com.solveria.core.experience.domain.model.Notification;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Secondary Port (Outbound): Repositorio de Notification. */
public interface NotificationPO {

  void save(Notification notification);

  Optional<Notification> findById(UUID notificationId);

  List<Notification> findByRecipientId(UUID recipientId);
}
