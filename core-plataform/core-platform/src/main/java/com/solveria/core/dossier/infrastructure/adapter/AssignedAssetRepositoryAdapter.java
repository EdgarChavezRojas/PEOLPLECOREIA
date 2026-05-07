package com.solveria.core.dossier.infrastructure.adapter;

import com.solveria.core.dossier.application.port.AssignedAssetRepositoryPort;
import com.solveria.core.dossier.domain.event.DossierEvent;
import com.solveria.core.dossier.domain.model.AssignedAsset;
import com.solveria.core.dossier.domain.model.vo.AssetStatus;
import com.solveria.core.dossier.infrastructure.jpa.AssignedAssetJpa;
import com.solveria.core.dossier.infrastructure.mapper.AssignedAssetMapper;
import com.solveria.core.dossier.infrastructure.repository.AssignedAssetRepository;
import com.solveria.core.security.context.SecurityTenantContext;
import com.solveria.core.shared.events.DomainEvent;
import com.solveria.core.workforce.application.port.EventOutboxPort;
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
    List<DomainEvent> events = asset.pullDomainEvents();
    AssignedAssetJpa jpa = assignedAssetMapper.toJpa(asset);
    AssignedAssetJpa savedJpa = assignedAssetRepository.save(jpa);
    AssignedAsset saved = assignedAssetMapper.toDomain(savedJpa);

    for (DomainEvent event : events) {
      if (event instanceof DossierEvent dossierEvent) {
        eventOutboxPort.publish(
            "AssignedAsset",
            saved.getAssignmentId(),
            dossierEvent.type().name(),
            assignedAssetMapper.toEventPayload(saved, dossierEvent));
      }
    }

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
}
