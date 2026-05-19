package com.solveria.core.accruals.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Payload para solicitar el pago de un quinquenio consolidado")
public record RequestQuinquenioPaymentRequest(

        @Schema(description = "ID de la relación laboral del empleado", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID relationshipId,

        @Schema(description = "Fecha en la que se realiza la solicitud formal", example = "2026-05-18")
        LocalDate requestDate
) {}