package com.solveria.TimeAndBearings.application.command;

import java.time.LocalDateTime;
import java.util.UUID;

/** Command to activate a device and install its public key (WF-TM04 step 2). */
public record ActivateDeviceCommand(
    UUID deviceId,
    UUID tenantId,
    String publicKeyPem,
    String firmwareVersion,
    LocalDateTime serverNtpNow,
    String actorId) {}
