package com.solveria.core.dossier.infrastructure.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solveria.core.dossier.domain.model.DocumentRecord;
import com.solveria.core.dossier.domain.model.vo.DocumentMetadata;
import com.solveria.core.dossier.domain.model.vo.ValidationStatus;
import com.solveria.core.dossier.infrastructure.jpa.DocumentMetadataEmbeddable;
import com.solveria.core.dossier.infrastructure.jpa.DocumentRecordJpa;
import com.solveria.core.dossier.infrastructure.jpa.ValidationStatusEmbeddable;
import com.solveria.core.shared.events.DomainEvent;
import java.util.Map;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface DocumentRecordMapper {

  DocumentRecordJpa toJpa(DocumentRecord record);

  ValidationStatusEmbeddable toEmbeddable(ValidationStatus status);

  DocumentMetadataEmbeddable toEmbeddable(DocumentMetadata metadata);

  default DocumentRecord toDomain(DocumentRecordJpa jpa) {
    if (jpa == null) {
      return null;
    }
    return new DocumentRecord(
        jpa.getDocId(),
        jpa.getRelationshipId(),
        jpa.getDocCategory(),
        jpa.getDocType(),
        Boolean.TRUE.equals(jpa.getCritical()),
        toDomain(jpa.getValidationStatus()),
        toDomain(jpa.getMetadata()),
        jpa.getTenantId(),
        Boolean.TRUE.equals(jpa.getExpirationWarningSent()));
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

  default String toEventPayload(DocumentRecord record, DomainEvent event) {
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
            "eventType", event.getClass().getSimpleName());
    try {
      return new ObjectMapper().writeValueAsString(payload);
    } catch (Exception e) {
      throw new RuntimeException("Error serializando DocumentRecord a JSON", e);
    }
  }
}
