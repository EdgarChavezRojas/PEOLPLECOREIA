package com.solveria.core.legal.application.dto;

import java.math.BigDecimal;

public record ComplianceSnapshotDto(
    BigDecimal smnApplied, String taxRegime, Boolean infocalActive) {}
