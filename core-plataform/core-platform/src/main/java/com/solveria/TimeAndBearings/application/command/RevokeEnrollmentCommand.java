package com.solveria.TimeAndBearings.application.command;

import com.solveria.TimeAndBearings.domain.model.enums.RevocationReason;
import java.time.LocalDateTime;
import java.util.UUID;

/** Command to revoke a biometric enrollment. */
public record RevokeEnrollmentCommand(
    UUID deviceId,
    UUID enrollmentId,
    UUID tenantId,
    LocalDateTime serverNtpNow,
    RevocationReason revocationReason) {}
