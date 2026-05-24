package com.solveria.payroll.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record DispersionFileResponse(
    UUID id,
    UUID payrollRunId,
    String bankCode,
    String status,
    BigDecimal totalAmount,
    Integer recordCount,
    LocalDateTime generatedAt) {}
