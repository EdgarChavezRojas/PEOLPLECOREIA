package com.solveria.core.dossier.domain.model;

import com.solveria.core.dossier.domain.event.DossierEvent;
import com.solveria.core.dossier.domain.event.DossierEventType;
import com.solveria.core.dossier.domain.exception.DossierErrorCode;
import com.solveria.core.dossier.domain.exception.InvalidAssetStateException;
import com.solveria.core.dossier.domain.model.vo.AssetDescriptor;
import com.solveria.core.dossier.domain.model.vo.AssetStatus;

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
public class AssignedAsset extends DomainRoot {

  private UUID assignmentId;
  private UUID workerId;
  private String assetTag;
  private AssetStatus status;
  private LocalDateTime assignedAt;
  private LocalDateTime returnedAt;
  private AssetDescriptor descriptor;
  private UUID tenantId;



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
    AssignedAsset asset =
        AssignedAsset.builder()
            .assignmentId(UUID.randomUUID())
            .workerId(workerId)
            .assetTag(assetTag)
            .status(AssetStatus.CUSTODY)
            .assignedAt(assignedAt != null ? assignedAt : LocalDateTime.now())
            .descriptor(descriptor)
            .tenantId(tenantId)
            .build();
    asset.registerEvent(DossierEvent.now(DossierEventType.ASSET_LOANED_TO_WORKER));
    return asset;
  }

  public void returnAsset(LocalDateTime returnedAt) {
    if (status != AssetStatus.CUSTODY) {
      throw new InvalidAssetStateException("El activo no esta en custodia");
    }
    this.status = AssetStatus.RETURNED;
    this.returnedAt = returnedAt != null ? returnedAt : LocalDateTime.now();
    registerEvent(DossierEvent.now(DossierEventType.ASSET_RETURNED));
  }

  public void reportDamage(LocalDateTime returnedAt) {
    if (status != AssetStatus.CUSTODY) {
      throw new InvalidAssetStateException("El activo no esta en custodia");
    }
    this.status = AssetStatus.DAMAGED;
    this.returnedAt = returnedAt != null ? returnedAt : LocalDateTime.now();
    registerEvent(DossierEvent.now(DossierEventType.ASSET_DAMAGE_REPORTED));
  }

  public void requestTransfer() {
    registerEvent(DossierEvent.now(DossierEventType.ASSET_TRANSFER_REQUIRED));
  }

  public void reportInspection(AssetDescriptor updatedDescriptor, boolean minorDamageReported) {
    if (updatedDescriptor == null) {
      throw new IllegalArgumentException(DossierErrorCode.ASSET_DESCRIPTOR_REQUIRED.name());
    }
    this.descriptor = updatedDescriptor;
    if (minorDamageReported) {
      registerEvent(DossierEvent.now(DossierEventType.ASSET_DAMAGE_REPORTED));
    }
  }


}
