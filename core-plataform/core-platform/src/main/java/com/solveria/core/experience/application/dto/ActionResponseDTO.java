package com.solveria.core.experience.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Respuesta general de una Acción de Autoservicio")
public record ActionResponseDTO(
        UUID actionId,
        String actionType,
        String status,
        String createdAt
) {}