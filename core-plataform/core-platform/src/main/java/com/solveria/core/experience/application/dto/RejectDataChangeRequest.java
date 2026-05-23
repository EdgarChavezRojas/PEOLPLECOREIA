package com.solveria.core.experience.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(description = "Payload para rechazar una solicitud de cambio de datos")
public record RejectDataChangeRequest(
    @Schema(description = "ID del aprobador", example = "123e4567-e89b-12d3-a456-426614174000")
        @NotNull UUID rejectedBy,
    @Schema(description = "Motivo del rechazo", example = "Datos incompletos")
        @NotBlank String rejectionReason) {}

