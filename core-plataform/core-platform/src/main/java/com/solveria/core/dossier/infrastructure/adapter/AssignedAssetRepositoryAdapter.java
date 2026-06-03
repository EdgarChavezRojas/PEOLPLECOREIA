package com.solveria.core.dossier.infrastructure.adapter;

import com.solveria.core.dossier.application.port.AssignedAssetRepositoryPort;
import com.solveria.core.dossier.domain.model.AssignedAsset;
import com.solveria.core.dossier.domain.model.vo.AssetStatus;
import com.solveria.core.dossier.infrastructure.jpa.AssignedAssetJpa;
import com.solveria.core.dossier.infrastructure.mapper.AssignedAssetMapper;
import com.solveria.core.dossier.infrastructure.repository.AssignedAssetRepository;
import com.solveria.core.security.context.SecurityTenantContext;
import com.solveria.core.shared.outbox.application.port.EventOutboxPort;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AssignedAssetRepositoryAdapter implements AssignedAssetRepositoryPort {

  private final AssignedAssetRepository assignedAssetRepository;
  private final AssignedAssetMapper assignedAssetMapper;
  private final EventOutboxPort eventOutboxPort;

  @Override
  @Transactional
  public AssignedAsset save(AssignedAsset asset) {
    // Idempotency: find existing assignment before creating a new one to avoid duplicates on
    // retries
    AssignedAssetJpa jpa =
        assignedAssetRepository
            .findByAssignmentIdAndTenantId(asset.getAssignmentId(), asset.getTenantId())
            .map(
                existing -> {
                  existing.setWorkerId(asset.getWorkerId());
                  existing.setAssetTag(asset.getAssetTag());
                  existing.setStatus(asset.getStatus());
                  existing.setAssignedAt(asset.getAssignedAt());
                  existing.setReturnedAt(asset.getReturnedAt());
                  existing.setDescriptor(assignedAssetMapper.toEmbeddable(asset.getDescriptor()));
                  return existing;
                })
            .orElseGet(() -> assignedAssetMapper.toJpa(asset));

    AssignedAssetJpa savedJpa = assignedAssetRepository.save(jpa);
    AssignedAsset saved = assignedAssetMapper.toDomain(savedJpa);

    eventOutboxPort.publish(asset.pullDomainEvents());

    return saved;
  }

  @Override
  public Optional<AssignedAsset> findById(UUID assignmentId) {
    String tenantId = SecurityTenantContext.getCurrentTenantId();
    return assignedAssetRepository
        .findByAssignmentIdAndTenantId(assignmentId, UUID.fromString(tenantId))
        .map(assignedAssetMapper::toDomain);
  }

  @Override
  public boolean hasPendingAssets(UUID workerId) {
    String tenantId = SecurityTenantContext.getCurrentTenantId();
    return assignedAssetRepository.existsByWorkerIdAndTenantIdAndStatusIn(
        workerId, UUID.fromString(tenantId), List.of(AssetStatus.CUSTODY));
  }

  @Override
  public List<UUID> findPendingAssetIds(UUID workerId) {
    return List.of();
  } // pendiente a revisar para que sirve esta funcionalidad

  @Override
  public boolean existsActiveAssignmentForWorkerAndTag(
      UUID workerId, String assetTag, UUID tenantId) {
    return assignedAssetRepository.existsByWorkerIdAndTenantIdAndAssetTagAndStatus(
        workerId, tenantId, assetTag, AssetStatus.CUSTODY);
  }
}
