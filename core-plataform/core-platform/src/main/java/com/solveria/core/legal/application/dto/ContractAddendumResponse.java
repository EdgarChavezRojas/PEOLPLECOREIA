package com.solveria.core.legal.application.dto;

import com.solveria.core.legal.domain.model.vo.AddendumStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ContractAddendumResponse(
    UUID addendumId,
    AddendumStatus status,
    LocalDate effectiveFrom,
    LocalDate effectiveTo,
    BigDecimal basicSalary,
    BigDecimal totalEarnedProj,
    BigDecimal netSalaryProj,
    String currency,
    BigDecimal smnApplied,
    String taxRegime,
    Boolean infocalActive) {}
