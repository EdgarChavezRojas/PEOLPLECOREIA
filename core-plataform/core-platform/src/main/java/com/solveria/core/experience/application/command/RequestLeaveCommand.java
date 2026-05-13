package com.solveria.core.experience.application.command;

import java.time.LocalDate;
import java.util.UUID;

/** Command: Solicitud de ausencia/permiso vía ESS. */
public record RequestLeaveCommand(
    UUID personId,
    String leaveType,
    LocalDate startDate,
    LocalDate endDate,
    String tenantId) {}
