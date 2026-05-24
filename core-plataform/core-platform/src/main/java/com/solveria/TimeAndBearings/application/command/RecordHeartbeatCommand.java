package com.solveria.TimeAndBearings.application.command;

import java.time.LocalDateTime;
import java.util.UUID;

/** Command to record a device heartbeat and sync state. */
public record RecordHeartbeatCommand(
    UUID deviceId,
    UUID tenantId,
    Integer batteryLevel,
    String syncStatusName,
    int enrolledCount,
    LocalDateTime serverNtpNow) {}
