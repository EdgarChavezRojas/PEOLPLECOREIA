package com.solveria.core.legal.application.dto.webRequest;

import com.solveria.core.legal.application.dto.ApproveContractAddendumRequest;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ApproveContractAddendumWebDto(
        @NotNull(message = "El contractId es obligatorio")
        UUID contractId,

        @NotNull(message = "El addendumId es obligatorio")
        UUID addendumId
) {
    public ApproveContractAddendumRequest toCommand(UUID tenantId) {
        return new ApproveContractAddendumRequest(
                this.contractId(),
                this.addendumId(),
                tenantId
        );
    }
}