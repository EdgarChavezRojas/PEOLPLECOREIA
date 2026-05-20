package com.solveria.core.experience.application.command;

import java.util.UUID;

/** Command: Solicitud de actualización de datos personales (W11 ESS). */
public record RequestDataUpdateCommand(UUID personId, String payload, String createdBy) {}
