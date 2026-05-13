package com.solveria.core.dossier.domain.model;

import com.solveria.core.dossier.domain.event.DocentAcademicTitleVerifiedEvent;
import com.solveria.core.dossier.domain.event.DocumentRecordedEvent;
import com.solveria.core.dossier.domain.event.DossierEvent;
import com.solveria.core.dossier.domain.event.DossierEventType;
import com.solveria.core.dossier.domain.exception.InvalidDocumentStateException;
import com.solveria.core.dossier.domain.model.vo.DocumentCategory;
import com.solveria.core.dossier.domain.model.vo.DocumentMetadata;
import com.solveria.core.dossier.domain.model.vo.ValidationState;
import com.solveria.core.dossier.domain.model.vo.ValidationStatus;
import com.solveria.core.dossier.domain.policy.DocumentCompliancePolicy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.solveria.core.shared.outbox.domain.DomainRoot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    DocumentRecord record =
        DocumentRecord.builder()
            .docId(UUID.randomUUID())
            .relationshipId(relationshipId)
            .docCategory(docCategory)
            .docType(docType)
            .critical(critical)
            .validationStatus(ValidationStatus.pending())
            .metadata(metadata)
            .tenantId(tenantId)
            .expirationWarningSent(false)
            .build();
    record.registerEvent(DossierEvent.now(DossierEventType.DOCUMENT_RECORDED));
    record.registerEvent(DocumentRecordedEvent.now(
        record.getDocId(), relationshipId, metadata.hashSha256()));
    return record;
  }

  public void approve(UUID reviewerId, LocalDateTime reviewDate) {
    ValidationState previous = validationStatus.currentState();
    this.validationStatus =
        validationStatus.withState(ValidationState.APPROVED, reviewerId, reviewDate, null);
    if (docCategory == DocumentCategory.ACADEMIC) {
      registerEvent(DossierEvent.now(DossierEventType.DOCENT_ACADEMIC_TITLE_VERIFIED));
      registerEvent(DocentAcademicTitleVerifiedEvent.now(
          relationshipId, docType, true));
    }
    if (previous == ValidationState.EXPIRED && critical) {
      registerEvent(DossierEvent.now(DossierEventType.ELIGIBILITY_RESTORED));
    }
  }

  public void reject(UUID reviewerId, String reason, LocalDateTime reviewDate) {
    this.validationStatus =
        validationStatus.withState(ValidationState.REJECTED, reviewerId, reviewDate, reason);
    registerEvent(DossierEvent.now(DossierEventType.DOCUMENT_VALIDATION_REJECTED));
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
          registerEvent(DossierEvent.now(DossierEventType.ELIGIBILITY_SUSPENDED_BY_COMPLIANCE));
        }
      }
    } else if (DocumentCompliancePolicy.isHealthCardExpiringSoon(
        docType, metadata.expiryDate(), today)) {
      registerEvent(DossierEvent.now(DossierEventType.HEALTH_CARD_EXPIRATION_WARNING));
    }
  }

  public void sendExpirationWarning() {
    if (expirationWarningSent) {
      return;
    }
    registerEvent(DossierEvent.now(DossierEventType.HEALTH_CARD_EXPIRATION_WARNING));
    this.expirationWarningSent = true;
  }

  public void registerDisciplinaryOutcome(boolean thresholdReached) {
    if (thresholdReached) {
      registerEvent(DossierEvent.now(DossierEventType.DISCIPLINARY_THRESHOLD_REACHED));
    } else {
      registerEvent(DossierEvent.now(DossierEventType.MEMORANDUM_ISSUED));
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
      registerEvent(DossierEvent.now(DossierEventType.MEMORANDUM_ACKNOWLEDGED));
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
      registerEvent(DossierEvent.now(DossierEventType.ELIGIBILITY_RESTORED));
    }
  }


}
