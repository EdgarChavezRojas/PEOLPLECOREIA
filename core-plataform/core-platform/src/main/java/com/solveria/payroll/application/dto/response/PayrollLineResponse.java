package com.solveria.payroll.application.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record PayrollLineResponse(
    UUID lineId,
    UUID employeeId,
    BigDecimal basicSalary,
    BigDecimal seniorityBonus,
    BigDecimal totalEarned,
    BigDecimal rcIvaRetained,
    BigDecimal gestoraRetained,
    BigDecimal infocalRetained,
    BigDecimal otherDeductions,
    BigDecimal netPayable) {}
