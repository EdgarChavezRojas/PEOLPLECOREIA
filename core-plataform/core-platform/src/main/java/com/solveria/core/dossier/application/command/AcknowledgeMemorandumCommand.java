package com.solveria.core.dossier.application.command;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record AcknowledgeMemorandumCommand(
    UUID documentId,
    boolean accepted,
    UUID reviewerId,
    LocalDateTime acknowledgedAt,
    byte[] signatureContent,
    String signatureFileName,
    LocalDate signatureExpiryDate,
    String location,
    UUID tenantId) {}
