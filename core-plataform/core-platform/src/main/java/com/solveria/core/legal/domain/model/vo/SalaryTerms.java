package com.solveria.core.legal.domain.model.vo;

import java.math.BigDecimal;

public record SalaryTerms(
    BigDecimal basicSalary,
    BigDecimal totalEarnedProj,
    BigDecimal netSalaryProj,
    String currency) {}
