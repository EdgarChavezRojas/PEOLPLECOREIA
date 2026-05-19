package com.solveria.core.legal.application.dto.webRequest;

import com.solveria.core.legal.application.dto.GenerateContractEvidenceRequest;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record GenerateContractEvidenceWebDto(
        @NotNull(message = "El contractId es obligatorio")
        UUID contractId
) {
    public GenerateContractEvidenceRequest toCommand(UUID tenantId) {
        return new GenerateContractEvidenceRequest(
                this.contractId(),
                tenantId
        );
    }
}