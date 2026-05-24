package com.solveria.core.legal.domain.model.vo;

import java.math.BigDecimal;

public record ComplianceSnapshot(BigDecimal smnApplied, String taxRegime, Boolean infocalActive) {}
