package com.solveria.TimeAndBearings.application.command;

import com.solveria.TimeAndBearings.domain.model.enums.DeviceRole;
import com.solveria.TimeAndBearings.domain.model.enums.DeviceType;
import java.time.LocalDateTime;
import java.util.UUID;

/** Command to register a new device in PROVISIONING state (WF-TM04 step 1). */
public record RegisterDeviceCommand(
    UUID tenantId,
    UUID orgUnitId,
    String serialNumber,
    DeviceType deviceType,
    DeviceRole deviceRole,
    boolean supportsFingerprint,
    boolean supportsFacial,
    boolean supportsNfc,
    boolean supportsQr,
    String firmwareVersion,
    LocalDateTime serverNtpNow,
    String actorId) {}
