package com.solveria.core.legal.application.dto;

import java.time.LocalDate;
import java.util.UUID;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.UUID;
public record ProposeContractAddendumRequest(
        UUID contractId,
        UUID addendumId,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,

        // El @Valid es CRÍTICO aquí para que Spring también valide por dentro estos sub-objetos
        @Valid @NotNull SalaryTermsDto salaryTerms,
        @Valid @NotNull ComplianceSnapshotDto complianceSnapshot,

        @NotBlank(message = "El tenantId es obligatorio")
        @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
                message = "Formato de tenantId inválido")
        String tenantId
) {}