package com.solveria.core.dossier.infrastructure.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solveria.core.dossier.domain.model.AssignedAsset;
import com.solveria.core.dossier.domain.model.vo.AssetDescriptor;
import com.solveria.core.dossier.infrastructure.jpa.AssetDescriptorEmbeddable;
import com.solveria.core.dossier.infrastructure.jpa.AssignedAssetJpa;
import com.solveria.core.shared.events.DomainEvent;
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
    return new AssignedAsset(
            jpa.getAssignmentId(),
            jpa.getWorkerId(),
            jpa.getAssetTag(),
            jpa.getStatus(),
            jpa.getAssignedAt(),
            jpa.getReturnedAt(),
            toDomain(jpa.getDescriptor()),
            jpa.getTenantId()
    );
  }

  default AssetDescriptor toDomain(AssetDescriptorEmbeddable embeddable) {
    if (embeddable == null) {
      return null;
    }
    return new AssetDescriptor(
        embeddable.getCategory(), embeddable.getTechSpecsJson(), embeddable.getInitialState());
  }

  default String toEventPayload(AssignedAsset asset, DomainEvent event) {
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
            "eventType", event.getClass().getSimpleName());
    try {
      return new ObjectMapper().writeValueAsString(payload);
    } catch (Exception e) {
      throw new RuntimeException("Error serializando AssignedAsset a JSON", e);
    }
  }
}
