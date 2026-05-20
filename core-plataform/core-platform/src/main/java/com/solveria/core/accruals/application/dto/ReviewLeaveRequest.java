package com.solveria.core.accruals.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Payload para que un Manager apruebe o rechace una solicitud de vacaciones")
public record ReviewLeaveRequest(
    @Schema(
            description = "ID del balance de vacaciones afectado",
            example = "123e4567-e89b-12d3-a456-426614174000")
        UUID balanceId) {}
