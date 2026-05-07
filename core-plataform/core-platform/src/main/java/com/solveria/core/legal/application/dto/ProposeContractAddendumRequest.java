package com.solveria.core.legal.application.dto;

import java.time.LocalDate;
import java.util.UUID;

public record ProposeContractAddendumRequest(
    UUID contractId,
    UUID addendumId,
    LocalDate effectiveFrom,
    LocalDate effectiveTo,
    SalaryTermsDto salaryTerms,
    ComplianceSnapshotDto complianceSnapshot,
    String tenantId) {}
