package com.solveria.core.dossier.infrastructure.repository;

import com.solveria.core.dossier.domain.model.vo.AssetStatus;
import com.solveria.core.dossier.infrastructure.jpa.AssignedAssetJpa;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssignedAssetRepository extends JpaRepository<AssignedAssetJpa, UUID> {

  Optional<AssignedAssetJpa> findByAssignmentIdAndTenantId(UUID assignmentId, UUID tenantId);

  boolean existsByWorkerIdAndTenantIdAndStatusIn(
      UUID workerId, UUID tenantId, List<AssetStatus> statuses);

  boolean existsByWorkerIdAndTenantIdAndAssetTagAndStatus(
      UUID workerId, UUID tenantId, String assetTag, AssetStatus status);
}
