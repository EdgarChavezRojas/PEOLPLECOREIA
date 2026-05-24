package com.solveria.payroll.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PayrollRunResponse(
    UUID id,
    UUID periodId,
    UUID tenantId,
    String runType,
    String status,
    BigDecimal totalGrossAmount,
    BigDecimal totalNetAmount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
