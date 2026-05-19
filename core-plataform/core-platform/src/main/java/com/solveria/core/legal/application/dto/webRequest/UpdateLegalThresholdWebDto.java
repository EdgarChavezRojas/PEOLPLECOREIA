package com.solveria.core.legal.application.dto.webRequest;

import com.solveria.core.legal.application.dto.UpdateLegalThresholdRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateLegalThresholdWebDto(
        @NotBlank(message = "El nombre de la regla (ruleName) es obligatorio")
        String ruleName,

        @NotNull(message = "El nuevo valor (newValue) es obligatorio")
        BigDecimal newValue,

        @NotBlank(message = "El userId es obligatorio")
        String userId
) {
    public UpdateLegalThresholdRequest toCommand(UUID tenantId) {
        return new UpdateLegalThresholdRequest(
                this.ruleName(),
                this.newValue(),
                tenantId,
                this.userId()
        );
    }
}