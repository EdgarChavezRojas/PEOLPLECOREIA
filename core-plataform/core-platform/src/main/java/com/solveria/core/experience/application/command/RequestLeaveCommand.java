package com.solveria.core.experience.application.command;

import java.time.LocalDate;

/** Command: Solicitud de ausencia/permiso vía ESS. */
public record RequestLeaveCommand(String leaveType, LocalDate startDate, LocalDate endDate) {}
