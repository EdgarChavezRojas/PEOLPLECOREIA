package com.solveria.TimeAndBearings.application.command;

import com.solveria.TimeAndBearings.domain.model.enums.AuthMethod;
import java.time.LocalDateTime;
import java.util.UUID;

/** Command to verify biometric authentication (P-TM29). */
public record VerifyBiometricAuthCommand(
    UUID deviceId,
    UUID tenantId,
    UUID relationshipId,
    AuthMethod authMethod,
    String deviceSignature,
    LocalDateTime serverNtpNow) {}
