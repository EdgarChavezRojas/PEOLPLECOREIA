package com.solveria.core.experience.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Payload para solicitud de actualización de datos")
public record DataUpdatePayloadDTO(
    @Schema(description = "Campo a actualizar", example = "ADDRESS") @NotBlank String fieldToUpdate,
    @Schema(description = "Nuevo valor", example = "Av. San Martín 123, Santa Cruz") @NotBlank
        String newValue,
    @Schema(description = "Justificación del cambio") String justification) {}
