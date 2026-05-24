package com.solveria.TimeAndBearings.application.command;

import java.time.LocalDateTime;
import java.util.UUID;

/** Command to decommission a device and revoke enrollments. */
public record DecommissionDeviceCommand(
    UUID deviceId, UUID tenantId, LocalDateTime serverNtpNow, String actorId) {}
