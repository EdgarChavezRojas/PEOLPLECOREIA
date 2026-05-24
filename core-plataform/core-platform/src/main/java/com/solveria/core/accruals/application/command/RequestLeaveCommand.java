package com.solveria.core.accruals.application.command;

import java.time.LocalDate;
import java.util.UUID;

public record RequestLeaveCommand(
    UUID balanceId, LocalDate startDate, LocalDate endDate, String location) {}
