package com.solveria.core.dossier.infrastructure.jpa;

import com.solveria.core.dossier.domain.model.vo.AssetStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "assigned_asset",
    indexes = {
      @Index(name = "idx_assigned_asset_worker", columnList = "worker_id"),
      @Index(name = "idx_assigned_asset_tenant", columnList = "tenant_id"),
      @Index(name = "idx_assigned_asset_status", columnList = "status")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignedAssetJpa {

  @Id
  @Column(name = "assignment_id")
  private UUID assignmentId;

  @Column(name = "worker_id", nullable = false)
  private UUID workerId;

  @Column(name = "asset_tag", nullable = false)
  private String assetTag;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private AssetStatus status;

  @Column(name = "assigned_at", nullable = false)
  private LocalDateTime assignedAt;

  @Column(name = "returned_at")
  private LocalDateTime returnedAt;

  @Embedded private AssetDescriptorEmbeddable descriptor;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;
}
