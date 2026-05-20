package com.solveria.core.legal.application.dto;

import java.time.LocalDate;
import java.util.UUID;

public record ProposeContractAddendumRequest(
    UUID contractId,
    UUID addendumId,
    LocalDate effectiveFrom,
    LocalDate effectiveTo,

    // El @Valid es CRÍTICO aquí para que Spring también valide por dentro estos sub-objetos
    SalaryTermsDto salaryTerms,
    ComplianceSnapshotDto complianceSnapshot,
    UUID tenantId) {}
