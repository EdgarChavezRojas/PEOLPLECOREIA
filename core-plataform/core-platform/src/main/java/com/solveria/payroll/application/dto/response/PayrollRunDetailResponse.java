package com.solveria.payroll.application.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record PayrollRunDetailResponse(
    UUID runId,
    UUID periodId,
    UUID tenantId,
    String runType,
    String status,
    BigDecimal totalGrossAmount,
    BigDecimal totalNetAmount,
    List<PayrollLineResponse> lines) {}
