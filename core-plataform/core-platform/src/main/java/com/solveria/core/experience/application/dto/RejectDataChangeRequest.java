package com.solveria.core.experience.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Payload para rechazar una solicitud de cambio de datos")
public record RejectDataChangeRequest(
    @Schema(description = "Motivo del rechazo", example = "Datos incompletos") @NotBlank
        String rejectionReason) {}
