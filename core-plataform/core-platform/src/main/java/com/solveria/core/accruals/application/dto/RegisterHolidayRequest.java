package com.solveria.core.accruals.application.dto;

import com.solveria.core.accruals.domain.model.vo.HolidayScope;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Payload para registrar un nuevo feriado")
public record RegisterHolidayRequest(

        @Schema(description = "Fecha exacta del feriado", example = "2026-09-24")
        LocalDate holidayDate,

        @Schema(description = "Alcance geográfico o departamental del feriado")
        HolidayScope scope
) {}