package com.solveria.core.dossier.infrastructure.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solveria.core.dossier.domain.event.DossierEvent;
import com.solveria.core.dossier.domain.model.AssignedAsset;
import com.solveria.core.dossier.domain.model.vo.AssetDescriptor;
import com.solveria.core.dossier.infrastructure.jpa.AssetDescriptorEmbeddable;
import com.solveria.core.dossier.infrastructure.jpa.AssignedAssetJpa;
import java.util.Map;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AssignedAssetMapper {

  AssignedAssetJpa toJpa(AssignedAsset asset);

  AssetDescriptorEmbeddable toEmbeddable(AssetDescriptor descriptor);

  default AssignedAsset toDomain(AssignedAssetJpa jpa) {
    if (jpa == null) {
      return null;
    }
    return AssignedAsset.builder()
        .assignmentId(jpa.getAssignmentId())
        .workerId(jpa.getWorkerId())
        .assetTag(jpa.getAssetTag())
        .status(jpa.getStatus())
        .assignedAt(jpa.getAssignedAt())
        .returnedAt(jpa.getReturnedAt())
        .descriptor(toDomain(jpa.getDescriptor()))
        .tenantId(jpa.getTenantId())
        .build();
  }

  default AssetDescriptor toDomain(AssetDescriptorEmbeddable embeddable) {
    if (embeddable == null) {
      return null;
    }
    return new AssetDescriptor(
        embeddable.getCategory(), embeddable.getTechSpecsJson(), embeddable.getInitialState());
  }

  default String toEventPayload(AssignedAsset asset, DossierEvent event) {
    if (asset == null || event == null) {
      return "{}";
    }
    Map<String, Object> payload =
        Map.of(
            "assignmentId", asset.getAssignmentId(),
            "workerId", asset.getWorkerId(),
            "tenantId", asset.getTenantId(),
            "assetTag", asset.getAssetTag(),
            "status", asset.getStatus() != null ? asset.getStatus().name() : null,
            "eventType", event.type().name());
    try {
      return new ObjectMapper().writeValueAsString(payload);
    } catch (Exception e) {
      throw new RuntimeException("Error serializando AssignedAsset a JSON", e);
    }
  }
}
