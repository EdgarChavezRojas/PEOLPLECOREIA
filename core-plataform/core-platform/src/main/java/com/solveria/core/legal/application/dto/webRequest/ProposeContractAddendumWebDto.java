package com.solveria.core.legal.application.dto.webRequest;

import com.solveria.core.legal.application.dto.ComplianceSnapshotDto;
import com.solveria.core.legal.application.dto.ProposeContractAddendumRequest;
import com.solveria.core.legal.application.dto.SalaryTermsDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record ProposeContractAddendumWebDto(
        @NotNull(message = "El contractId es obligatorio")
        UUID contractId,

        @NotNull(message = "El addendumId es obligatorio")
        UUID addendumId,

        @NotNull(message = "La fecha effectiveFrom es obligatoria")
        LocalDate effectiveFrom,

        LocalDate effectiveTo,

        @Valid @NotNull(message = "salaryTerms es obligatorio")
        SalaryTermsDto salaryTerms,

        @Valid @NotNull(message = "complianceSnapshot es obligatorio")
        ComplianceSnapshotDto complianceSnapshot
) {
    // Método de transformación (Factory Method)
    public ProposeContractAddendumRequest toCommand(UUID tenantId) {
        return new ProposeContractAddendumRequest(
                this.contractId(),
                this.addendumId(),
                this.effectiveFrom(),
                this.effectiveTo(),
                this.salaryTerms(),
                this.complianceSnapshot(),
                tenantId
        );
    }
}