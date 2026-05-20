package com.solveria.TimeAndBearings.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/** Per-employee daily attendance summary for roster event emission. */
public record EmployeeDailySummary(
    UUID relationshipId, BigDecimal totalHours, BigDecimal attendanceRateLast30d) {}
