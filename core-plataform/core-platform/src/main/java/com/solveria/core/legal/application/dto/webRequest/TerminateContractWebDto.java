package com.solveria.core.legal.application.dto.webRequest;

import com.solveria.core.legal.application.dto.TerminateContractRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TerminateContractWebDto(
        @NotNull(message = "El contractId es obligatorio")
        UUID contractId,

        @NotBlank(message = "El reason es obligatorio")
        String reason
) {
    public TerminateContractRequest toCommand(UUID tenantId) {
        return new TerminateContractRequest(
                this.contractId(),
                tenantId,
                this.reason()
        );
    }
}