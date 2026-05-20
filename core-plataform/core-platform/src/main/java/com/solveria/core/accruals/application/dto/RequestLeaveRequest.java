package com.solveria.core.accruals.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Payload para que un empleado solicite vacaciones")
public record RequestLeaveRequest(
    @Schema(
            description = "ID del balance de vacaciones del empleado",
            example = "123e4567-e89b-12d3-a456-426614174000")
        UUID balanceId,
    @Schema(description = "Fecha de inicio de las vacaciones", example = "2026-10-01")
        LocalDate startDate,
    @Schema(description = "Fecha de finalización de las vacaciones", example = "2026-10-15")
        LocalDate endDate) {}
