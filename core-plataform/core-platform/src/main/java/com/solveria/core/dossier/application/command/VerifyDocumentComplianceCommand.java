package com.solveria.core.dossier.application.command;

import com.solveria.core.dossier.domain.model.vo.DocumentCategory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record VerifyDocumentComplianceCommand(
    UUID docId,
    UUID relationshipId,
    DocumentCategory docCategory,
    String docType,
    boolean critical,
    UUID storageId,
    String fileName,
    byte[] fileContent,
    LocalDate expiryDate,
    ComplianceDecision decision,
    UUID reviewerId,
    String rejectReason,
    LocalDateTime reviewDate,
    String location,
    UUID tenantId,
    String tenantSegment) {}
