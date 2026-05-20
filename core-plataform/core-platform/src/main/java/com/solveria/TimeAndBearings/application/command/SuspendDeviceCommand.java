package com.solveria.TimeAndBearings.application.command;

import java.time.LocalDateTime;
import java.util.UUID;

/** Command to suspend a device temporarily. */
public record SuspendDeviceCommand(
    UUID deviceId, UUID tenantId, LocalDateTime serverNtpNow, String actorId, String reason) {}
