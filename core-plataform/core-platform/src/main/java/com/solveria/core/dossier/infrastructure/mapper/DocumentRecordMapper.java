package com.solveria.core.dossier.infrastructure.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solveria.core.dossier.domain.event.DossierEvent;
import com.solveria.core.dossier.domain.model.DocumentRecord;
import com.solveria.core.dossier.domain.model.vo.DocumentMetadata;
import com.solveria.core.dossier.domain.model.vo.ValidationStatus;
import com.solveria.core.dossier.infrastructure.jpa.DocumentMetadataEmbeddable;
import com.solveria.core.dossier.infrastructure.jpa.DocumentRecordJpa;
import com.solveria.core.dossier.infrastructure.jpa.ValidationStatusEmbeddable;
import java.util.Map;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DocumentRecordMapper {

  DocumentRecordJpa toJpa(DocumentRecord record);

  ValidationStatusEmbeddable toEmbeddable(ValidationStatus status);

  DocumentMetadataEmbeddable toEmbeddable(DocumentMetadata metadata);

  default DocumentRecord toDomain(DocumentRecordJpa jpa) {
    if (jpa == null) {
      return null;
    }
    return DocumentRecord.builder()
        .docId(jpa.getDocId())
        .relationshipId(jpa.getRelationshipId())
        .docCategory(jpa.getDocCategory())
        .docType(jpa.getDocType())
        .critical(Boolean.TRUE.equals(jpa.getCritical()))
        .validationStatus(toDomain(jpa.getValidationStatus()))
        .metadata(toDomain(jpa.getMetadata()))
        .tenantId(jpa.getTenantId())
        .expirationWarningSent(Boolean.TRUE.equals(jpa.getExpirationWarningSent()))
        .build();
  }

  default ValidationStatus toDomain(ValidationStatusEmbeddable embeddable) {
    if (embeddable == null) {
      return null;
    }
    return new ValidationStatus(
        embeddable.getCurrentState(),
        embeddable.getReviewerId(),
        embeddable.getReviewDate(),
        embeddable.getRejectReason());
  }

  default DocumentMetadata toDomain(DocumentMetadataEmbeddable embeddable) {
    if (embeddable == null) {
      return null;
    }
    return new DocumentMetadata(
        embeddable.getStorageId(),
        embeddable.getFileName(),
        embeddable.getHashSha256(),
        embeddable.getExpiryDate());
  }

  default String toEventPayload(DocumentRecord record, DossierEvent event) {
    if (record == null || event == null) {
      return "{}";
    }
    Map<String, Object> payload =
        Map.of(
            "docId", record.getDocId(),
            "relationshipId", record.getRelationshipId(),
            "tenantId", record.getTenantId(),
            "docCategory", record.getDocCategory() != null ? record.getDocCategory().name() : null,
            "docType", record.getDocType(),
            "status",
                record.getValidationStatus() != null
                        && record.getValidationStatus().currentState() != null
                    ? record.getValidationStatus().currentState().name()
                    : null,
            "eventType", event.type().name());
    try {
      return new ObjectMapper().writeValueAsString(payload);
    } catch (Exception e) {
      throw new RuntimeException("Error serializando DocumentRecord a JSON", e);
    }
  }
}
