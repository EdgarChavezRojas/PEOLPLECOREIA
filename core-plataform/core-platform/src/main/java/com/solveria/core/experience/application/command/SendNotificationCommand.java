package com.solveria.core.experience.application.command;

import java.util.UUID;

/** Command: Enviar notificación a un destinatario. */
public record SendNotificationCommand(
    UUID recipientId, String channel, String subject, String body, String tenantId) {}
