package com.solveria.TimeAndBearings.application.command;

import com.solveria.TimeAndBearings.domain.model.enums.BiometricType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/** Command to enroll a biometric template for a collaborator (WF-TM04). */
public record EnrollBiometricCommand(
    UUID deviceId,
    UUID tenantId,
    UUID relationshipId,
    BiometricType biometricType,
    String templateHash,
    BigDecimal templateQualityScore,
    LocalDateTime serverNtpNow) {}
