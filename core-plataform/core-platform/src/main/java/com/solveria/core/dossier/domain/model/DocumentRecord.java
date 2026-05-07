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
import com.solveria.core.shared.events.DomainEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentRecord {

  private UUID docId;
  private UUID relationshipId;
  private DocumentCategory docCategory;
  private String docType;
  private boolean critical;
  private ValidationStatus validationStatus;
  private DocumentMetadata metadata;
  private UUID tenantId;

  @Builder.Default private transient List<DomainEvent> domainEvents = new ArrayList<>();

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
            .build();
    record.addDomainEvent(DossierEvent.now(DossierEventType.DOCUMENT_RECORDED));
    record.addDomainEvent(DocumentRecordedEvent.now(
        record.getDocId(), relationshipId, metadata.hashSha256()));
    return record;
  }

  public void approve(UUID reviewerId, LocalDateTime reviewDate) {
    ValidationState previous = validationStatus.currentState();
    this.validationStatus =
        validationStatus.withState(ValidationState.APPROVED, reviewerId, reviewDate, null);
    if (docCategory == DocumentCategory.ACADEMIC) {
      addDomainEvent(DossierEvent.now(DossierEventType.DOCENT_ACADEMIC_TITLE_VERIFIED));
      addDomainEvent(DocentAcademicTitleVerifiedEvent.now(
          relationshipId, docType, true));
    }
    if (previous == ValidationState.EXPIRED && critical) {
      addDomainEvent(DossierEvent.now(DossierEventType.ELIGIBILITY_RESTORED));
    }
  }

  public void reject(UUID reviewerId, String reason, LocalDateTime reviewDate) {
    this.validationStatus =
        validationStatus.withState(ValidationState.REJECTED, reviewerId, reviewDate, reason);
    addDomainEvent(DossierEvent.now(DossierEventType.DOCUMENT_VALIDATION_REJECTED));
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
          addDomainEvent(DossierEvent.now(DossierEventType.ELIGIBILITY_SUSPENDED_BY_COMPLIANCE));
        }
      }
    } else if (DocumentCompliancePolicy.isHealthCardExpiringSoon(
        docType, metadata.expiryDate(), today)) {
      addDomainEvent(DossierEvent.now(DossierEventType.HEALTH_CARD_EXPIRATION_WARNING));
    }
  }

  public void restore(UUID reviewerId, LocalDateTime reviewDate) {
    if (validationStatus.currentState() != ValidationState.EXPIRED) {
      throw new InvalidDocumentStateException("Solo se puede restaurar desde estado EXPIRED");
    }
    this.validationStatus =
        validationStatus.withState(ValidationState.APPROVED, reviewerId, reviewDate, null);
    if (critical) {
      addDomainEvent(DossierEvent.now(DossierEventType.ELIGIBILITY_RESTORED));
    }
  }

  public void addDomainEvent(DomainEvent event) {
    if (domainEvents == null) {
      domainEvents = new ArrayList<>();
    }
    domainEvents.add(event);
  }

  public List<DomainEvent> pullDomainEvents() {
    if (domainEvents == null || domainEvents.isEmpty()) {
      return List.of();
    }
    List<DomainEvent> events = List.copyOf(domainEvents);
    domainEvents.clear();
    return events;
  }
}
