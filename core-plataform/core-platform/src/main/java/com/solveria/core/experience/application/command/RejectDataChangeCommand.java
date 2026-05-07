package com.solveria.core.experience.application.command;

import java.util.UUID;

/** Command: Rechazo MSS de solicitud de cambio de datos (W11). */
public record RejectDataChangeCommand(
    UUID actionId, UUID rejectedBy, String rejectionReason, String tenantId) {}
