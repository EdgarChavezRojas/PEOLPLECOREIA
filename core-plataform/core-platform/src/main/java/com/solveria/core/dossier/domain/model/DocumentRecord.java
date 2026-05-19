package com.solveria.core.dossier.domain.model;

import com.solveria.core.dossier.domain.event.DocentAcademicTitleVerifiedEvent;
import com.solveria.core.dossier.domain.event.DisciplinaryThresholdReachedEvent;
import com.solveria.core.dossier.domain.event.DocumentRecordedEvent;
import com.solveria.core.dossier.domain.event.DocumentValidationRejectedEvent;
import com.solveria.core.dossier.domain.event.EligibilityRestoredEvent;
import com.solveria.core.dossier.domain.event.EligibilitySuspendedByComplianceEvent;
import com.solveria.core.dossier.domain.event.HealthCardExpirationWarningEvent;
import com.solveria.core.dossier.domain.event.MemorandumIssuedEvent;
import com.solveria.core.dossier.domain.exception.InvalidDocumentStateException;
import com.solveria.core.dossier.domain.model.vo.DocumentCategory;
import com.solveria.core.dossier.domain.model.vo.DocumentMetadata;
import com.solveria.core.dossier.domain.model.vo.ValidationState;
import com.solveria.core.dossier.domain.model.vo.ValidationStatus;
import com.solveria.core.dossier.domain.policy.DocumentCompliancePolicy;
import com.solveria.core.shared.events.MemorandumAcknowledgedEvent;
import com.solveria.core.shared.outbox.domain.DomainRoot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class DocumentRecord extends DomainRoot {

  private UUID docId;
  private UUID relationshipId;
  private DocumentCategory docCategory;
  private String docType;
  private boolean critical;
  private ValidationStatus validationStatus;
  private DocumentMetadata metadata;
  private UUID tenantId;
  private boolean expirationWarningSent;

  public DocumentRecord() {
  }

  public DocumentRecord(UUID docId, UUID relationshipId, DocumentCategory docCategory, String docType,
                        boolean critical, ValidationStatus validationStatus, DocumentMetadata metadata,
                        UUID tenantId, boolean expirationWarningSent) {
    this.docId = docId;
    this.relationshipId = relationshipId;
    this.docCategory = docCategory;
    this.docType = docType;
    this.critical = critical;
    this.validationStatus = validationStatus;
    this.metadata = metadata;
    this.tenantId = tenantId;
    this.expirationWarningSent = expirationWarningSent;
  }

  public static DocumentRecord record(
          UUID relationshipId,
          DocumentCategory docCategory,
          String docType,
          boolean critical,
          DocumentMetadata metadata,
          UUID tenantId) {
    if (relationshipId == null) {
      throw new IllegalArgumentException("relationshipId es requerido");
    }
    if (docCategory == null) {
      throw new IllegalArgumentException("docCategory es requerido");
    }
    if (docType == null || docType.isBlank()) {
      throw new IllegalArgumentException("docType es requerido");
    }
    if (metadata == null) {
      throw new IllegalArgumentException("metadata es requerido");
    }
    if (tenantId == null) {
      throw new IllegalArgumentException("tenantId es requerido");
    }

    DocumentRecord record = new DocumentRecord(
            UUID.randomUUID(),
            relationshipId,
            docCategory,
            docType,
            critical,
            ValidationStatus.pending(),
            metadata,
            tenantId,
            false
    );

    record.registerEvent(
            DocumentRecordedEvent.now(record.getDocId(), relationshipId, metadata.hashSha256()));
    return record;
  }

  public void approve(UUID reviewerId, LocalDateTime reviewDate) {
    ValidationState previous = validationStatus.currentState();
    this.validationStatus =
            validationStatus.withState(ValidationState.APPROVED, reviewerId, reviewDate, null);
    if (docCategory == DocumentCategory.ACADEMIC) {
      registerEvent(DocentAcademicTitleVerifiedEvent.now(relationshipId, docType, true));
    }
    if (previous == ValidationState.EXPIRED && critical) {
      registerEvent(EligibilityRestoredEvent.now(relationshipId));
    }
  }

  public void reject(UUID reviewerId, String reason, LocalDateTime reviewDate) {
    this.validationStatus =
            validationStatus.withState(ValidationState.REJECTED, reviewerId, reviewDate, reason);
    registerEvent(DocumentValidationRejectedEvent.now(docId, relationshipId, reason));
  }

  public void evaluateExpiration(LocalDate today) {
    if (metadata == null || metadata.expiryDate() == null || today == null) {
      return;
    }
    if (!metadata.expiryDate().isAfter(today)) {
      if (validationStatus.currentState() != ValidationState.EXPIRED) {
        this.validationStatus =
                validationStatus.withState(ValidationState.EXPIRED, null, null, null);
        if (critical) {
          registerEvent(EligibilitySuspendedByComplianceEvent.now(relationshipId));
        }
      }
    } else if (DocumentCompliancePolicy.isHealthCardExpiringSoon(
            docType, metadata.expiryDate(), today)) {
      registerEvent(HealthCardExpirationWarningEvent.now(docId, relationshipId));
    }
  }

  public void sendExpirationWarning() {
    if (expirationWarningSent) {
      return;
    }
    registerEvent(HealthCardExpirationWarningEvent.now(docId, relationshipId));
    this.expirationWarningSent = true;
  }

  public void registerDisciplinaryOutcome(boolean thresholdReached) {
    if (thresholdReached) {
      registerEvent(DisciplinaryThresholdReachedEvent.now(relationshipId));
    } else {
      registerEvent(MemorandumIssuedEvent.now(relationshipId));
    }
  }

  public void acknowledgeMemorandum(
          boolean accepted,
          UUID reviewerId,
          LocalDateTime acknowledgedAt,
          String witnessRequiredReason) {
    if (accepted) {
      this.validationStatus =
              validationStatus.withState(ValidationState.APPROVED, reviewerId, acknowledgedAt, null);
      registerEvent(MemorandumAcknowledgedEvent.now(relationshipId));
      return;
    }
    this.validationStatus =
            validationStatus.withState(
                    ValidationState.REJECTED, reviewerId, acknowledgedAt, witnessRequiredReason);
  }

  public void restore(UUID reviewerId, LocalDateTime reviewDate) {
    if (validationStatus.currentState() != ValidationState.EXPIRED) {
      throw new InvalidDocumentStateException("Solo se puede restaurar desde estado EXPIRED");
    }
    this.validationStatus =
            validationStatus.withState(ValidationState.APPROVED, reviewerId, reviewDate, null);
    if (critical) {
      registerEvent(EligibilityRestoredEvent.now(relationshipId));
    }
  }

  public UUID getDocId() {
    return docId;
  }

  public UUID getRelationshipId() {
    return relationshipId;
  }

  public DocumentCategory getDocCategory() {
    return docCategory;
  }

  public String getDocType() {
    return docType;
  }

  public boolean isCritical() {
    return critical;
  }

  public ValidationStatus getValidationStatus() {
    return validationStatus;
  }

  public DocumentMetadata getMetadata() {
    return metadata;
  }

  public UUID getTenantId() {
    return tenantId;
  }

  public boolean isExpirationWarningSent() {
    return expirationWarningSent;
  }
}