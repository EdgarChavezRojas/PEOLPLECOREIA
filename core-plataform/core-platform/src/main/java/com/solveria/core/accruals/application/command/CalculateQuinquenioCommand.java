package com.solveria.core.accruals.application.command;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CalculateQuinquenioCommand(
    UUID relationshipId,
    int monthsCompleted,
    BigDecimal averageLast90Days,
    LocalDate requestDate,
    LocalDate paymentDate,
    LocalDate today,
    String location,
    String tenantSegment) {}
