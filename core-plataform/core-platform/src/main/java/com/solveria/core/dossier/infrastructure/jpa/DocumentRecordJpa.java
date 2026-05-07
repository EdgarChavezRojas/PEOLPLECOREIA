package com.solveria.core.dossier.infrastructure.jpa;

import com.solveria.core.dossier.domain.model.vo.DocumentCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "document_record",
    indexes = {
      @Index(name = "idx_document_record_relationship", columnList = "relationship_id"),
      @Index(name = "idx_document_record_tenant", columnList = "tenant_id"),
      @Index(name = "idx_document_record_category", columnList = "doc_category")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentRecordJpa {

  @Id
  @Column(name = "doc_id")
  private UUID docId;

  @Column(name = "relationship_id", nullable = false)
  private UUID relationshipId;

  @Enumerated(EnumType.STRING)
  @Column(name = "doc_category", nullable = false)
  private DocumentCategory docCategory;

  @Column(name = "doc_type", nullable = false)
  private String docType;

  @Column(name = "is_critical", nullable = false)
  private Boolean critical;

  @Embedded private ValidationStatusEmbeddable validationStatus;

  @Embedded private DocumentMetadataEmbeddable metadata;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;
}
