package com.solveria.core.experience.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Respuesta de Notificación")
public record NotificationResponseDTO(
        UUID notificationId,
        String channel,
        String content,
        boolean isRead,
        String createdAt
) {}