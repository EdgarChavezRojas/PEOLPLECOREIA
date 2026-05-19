package com.solveria.core.experience.infrastructure.jpa;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** JPA Entity: SelfServiceAction. Tabla: experience_self_service_action. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "experience_self_service_action")
public class SelfServiceActionJpa {

  @Id
  @Column(name = "action_id", nullable = false, updatable = false)
  private UUID actionId;

  @Column(name = "person_id", nullable = false)
  private UUID personId;

  @Column(name = "action_type", length = 30, nullable = false)
  @Enumerated(EnumType.STRING)
  private com.solveria.core.experience.domain.model.vo.ActionType actionType;

  @Column(name = "payload", columnDefinition = "TEXT")
  private String payload;

  @Column(name = "tenant_id", length = 50, nullable = false)
  private UUID tenantId;

  @Column(name = "created_by", length = 100, nullable = false)
  private String createdBy;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  // Certificate payload fields (W14)
  @Column(name = "cert_type", length = 50)
  private String certType;

  @Column(name = "cert_pdf_content", columnDefinition = "TEXT")
  private String certPdfContent;

  @Column(name = "cert_sha256_hash", length = 64)
  private String certSha256Hash;

  @Column(name = "cert_qr_url", length = 512)
  private String certQrUrl;

  @Column(name = "cert_generated_at")
  private Instant certGeneratedAt;

  @OneToOne(
      mappedBy = "selfServiceAction",
      cascade = CascadeType.ALL,
      fetch = FetchType.LAZY,
      orphanRemoval = true)
  private ApprovalWorkflowJpa approvalWorkflow;
}
