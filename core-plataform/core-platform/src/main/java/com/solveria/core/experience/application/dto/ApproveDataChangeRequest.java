package com.solveria.core.experience.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(description = "Payload para aprobar una solicitud de cambio de datos")
public record ApproveDataChangeRequest(
    @Schema(description = "ID del aprobador", example = "123e4567-e89b-12d3-a456-426614174000")
        @NotNull
        UUID approvedBy) {}
