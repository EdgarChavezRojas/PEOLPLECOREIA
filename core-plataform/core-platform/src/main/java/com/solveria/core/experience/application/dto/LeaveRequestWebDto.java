package com.solveria.core.experience.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Schema(description = "Payload para solicitar una licencia/ausencia desde ESS")
public record LeaveRequestWebDto(
    @Schema(description = "Tipo de ausencia", example = "ANNUAL_LEAVE") @NotBlank String leaveType,
    @Schema(description = "Fecha de inicio", example = "2026-10-01") @NotNull LocalDate startDate,
    @Schema(description = "Fecha de fin", example = "2026-10-15") @NotNull LocalDate endDate) {}
