package com.solveria.core.dossier.domain.model;

import com.solveria.core.dossier.domain.event.AssetDamageReportedEvent;
import com.solveria.core.dossier.domain.event.AssetLoanedToWorkerEvent;
import com.solveria.core.dossier.domain.event.AssetReturnedEvent;
import com.solveria.core.dossier.domain.event.AssetTransferRequiredEvent;
import com.solveria.core.dossier.domain.exception.DossierErrorCode;
import com.solveria.core.dossier.domain.exception.InvalidAssetStateException;
import com.solveria.core.dossier.domain.model.vo.AssetDescriptor;
import com.solveria.core.dossier.domain.model.vo.AssetStatus;
import com.solveria.core.shared.outbox.domain.DomainRoot;

import java.time.LocalDateTime;
import java.util.UUID;

public class AssignedAsset extends DomainRoot {

  private UUID assignmentId;
  private UUID workerId;
  private String assetTag;
  private AssetStatus status;
  private LocalDateTime assignedAt;
  private LocalDateTime returnedAt;
  private AssetDescriptor descriptor;
  private UUID tenantId;

  public AssignedAsset() {
  }

  public AssignedAsset(UUID assignmentId, UUID workerId, String assetTag, AssetStatus status,
                       LocalDateTime assignedAt, LocalDateTime returnedAt, AssetDescriptor descriptor, UUID tenantId) {
    this.assignmentId = assignmentId;
    this.workerId = workerId;
    this.assetTag = assetTag;
    this.status = status;
    this.assignedAt = assignedAt;
    this.returnedAt = returnedAt;
    this.descriptor = descriptor;
    this.tenantId = tenantId;
  }

  public static AssignedAsset assign(
          UUID workerId,
          String assetTag,
          AssetDescriptor descriptor,
          LocalDateTime assignedAt,
          UUID tenantId) {
    if (workerId == null) {
      throw new IllegalArgumentException("workerId es requerido");
    }
    if (assetTag == null || assetTag.isBlank()) {
      throw new IllegalArgumentException("assetTag es requerido");
    }
    if (descriptor == null) {
      throw new IllegalArgumentException("descriptor es requerido");
    }
    if (tenantId == null) {
      throw new IllegalArgumentException("tenantId es requerido");
    }

    AssignedAsset asset = new AssignedAsset(
            UUID.randomUUID(),
            workerId,
            assetTag,
            AssetStatus.CUSTODY,
            assignedAt != null ? assignedAt : LocalDateTime.now(),
            null,
            descriptor,
            tenantId
    );

    asset.registerEvent(AssetLoanedToWorkerEvent.now(asset.getAssignmentId(), asset.getWorkerId()));
    return asset;
  }

  public void returnAsset(LocalDateTime returnedAt) {
    if (status != AssetStatus.CUSTODY) {
      throw new InvalidAssetStateException("El activo no esta en custodia");
    }
    this.status = AssetStatus.RETURNED;
    this.returnedAt = returnedAt != null ? returnedAt : LocalDateTime.now();
    registerEvent(AssetReturnedEvent.now(this.assignmentId, this.workerId));
  }

  public void reportDamage(LocalDateTime returnedAt) {
    if (status != AssetStatus.CUSTODY) {
      throw new InvalidAssetStateException("El activo no esta en custodia");
    }
    this.status = AssetStatus.DAMAGED;
    this.returnedAt = returnedAt != null ? returnedAt : LocalDateTime.now();
    registerEvent(AssetDamageReportedEvent.now(this.assignmentId, this.workerId));
  }

  public void requestTransfer() {
    registerEvent(AssetTransferRequiredEvent.now(this.assignmentId, this.workerId));
  }

  public void reportInspection(AssetDescriptor updatedDescriptor, boolean minorDamageReported) {
    if (updatedDescriptor == null) {
      throw new IllegalArgumentException(DossierErrorCode.ASSET_DESCRIPTOR_REQUIRED.name());
    }
    this.descriptor = updatedDescriptor;
    if (minorDamageReported) {
      registerEvent(AssetDamageReportedEvent.now(this.assignmentId, this.workerId));
    }
  }

  public UUID getAssignmentId() {
    return assignmentId;
  }

  public UUID getWorkerId() {
    return workerId;
  }

  public String getAssetTag() {
    return assetTag;
  }

  public AssetStatus getStatus() {
    return status;
  }

  public LocalDateTime getAssignedAt() {
    return assignedAt;
  }

  public LocalDateTime getReturnedAt() {
    return returnedAt;
  }

  public AssetDescriptor getDescriptor() {
    return descriptor;
  }

  public UUID getTenantId() {
    return tenantId;
  }
}