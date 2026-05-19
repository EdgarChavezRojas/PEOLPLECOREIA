package com.solveria.core.experience.application.command;

import java.util.UUID;

/** Command: Aprobación MSS de solicitud de cambio de datos (W11). */
public record ApproveDataChangeCommand(UUID actionId, UUID approvedBy) {}
