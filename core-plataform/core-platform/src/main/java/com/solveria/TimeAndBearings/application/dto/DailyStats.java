package com.solveria.TimeAndBearings.application.dto;

import java.math.BigDecimal;

/** Aggregated daily attendance statistics for one OrgUnit and one workDate. */
public record DailyStats(
    int totalScheduled,
    int totalAttended,
    int totalNoShows,
    int totalExceptionsPending,
    BigDecimal totalRegularHours,
    BigDecimal totalOvertimeHours,
    BigDecimal totalNightHours) {}
