package com.solveria.core.legal.application.dto;

import java.math.BigDecimal;

public record SalaryTermsDto(
    BigDecimal basicSalary,
    BigDecimal totalEarnedProj,
    BigDecimal netSalaryProj,
    String currency) {}
