package com.solveria.TimeAndBearings.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/** Accumulated worked-hours summary for a single employee across the full period. */
public record EmployeePeriodSummary(
    UUID relationshipId,
    BigDecimal regularHoursTotal,
    BigDecimal overtimeHoursTotal,
    BigDecimal nightHoursTotal,
    BigDecimal holidayHoursTotal,
    int unjustifiedAbsences,
    int remoteWorkDays,
    boolean hadAutoClosedLedgers) {}
