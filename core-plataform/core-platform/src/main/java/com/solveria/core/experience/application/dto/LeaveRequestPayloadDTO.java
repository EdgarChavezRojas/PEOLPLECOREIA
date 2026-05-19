package com.solveria.core.experience.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Schema(description = "Payload para solicitud de vacaciones")
public record LeaveRequestPayloadDTO(
        @Schema(description = "Fecha de inicio")
        @NotNull LocalDate startDate,

        @Schema(description = "Fecha de fin")
        @NotNull LocalDate endDate,

        @Schema(description = "Tipo de ausencia", example = "ANNUAL_LEAVE")
        @NotBlank String leaveType
) {}