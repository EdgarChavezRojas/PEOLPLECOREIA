package com.solveria.core.experience.application.port.in;

import java.util.UUID;

public interface NotificationPI {
    UUID send(UUID recipientId, String channel, String subject, String body, UUID tenantId);

    void markAsRead(UUID notificationId);
}
