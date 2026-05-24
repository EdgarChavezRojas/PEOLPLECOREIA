package com.solveria.core.accruals.application.command;

import java.time.LocalDate;
import java.util.UUID;

public record AccrueVacationCommand(
    UUID balanceId, int yearsOfService, LocalDate accrualDate, String location) {}
