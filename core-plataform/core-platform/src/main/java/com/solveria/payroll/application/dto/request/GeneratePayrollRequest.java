package com.solveria.payroll.application.dto.request;

import java.util.UUID;

public record GeneratePayrollRequest(
    UUID periodId,
    String runType,
    String description
) {}
