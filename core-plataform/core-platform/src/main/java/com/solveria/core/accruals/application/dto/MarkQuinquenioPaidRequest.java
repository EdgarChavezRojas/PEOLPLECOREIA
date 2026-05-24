package com.solveria.core.accruals.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "Payload para que Finanzas confirme que el quinquenio fue desembolsado")
public record MarkQuinquenioPaidRequest(
    @Schema(
            description = "Fecha exacta en la que se realizó la transferencia o pago",
            example = "2026-05-20")
        LocalDate paymentDate) {}
