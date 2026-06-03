package com.solveria.core.dossier.application.port;

import com.solveria.core.dossier.domain.model.AssignedAsset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssignedAssetRepositoryPort {

  AssignedAsset save(AssignedAsset asset);

  Optional<AssignedAsset> findById(UUID assignmentId);

  boolean hasPendingAssets(UUID workerId);

  List<UUID> findPendingAssetIds(UUID workerId);

  /**
   * Verifica si el activo (por assetTag) ya está asignado a ese worker en estado ASSIGNED/CUSTODY
   */
  boolean existsActiveAssignmentForWorkerAndTag(UUID workerId, String assetTag, UUID tenantId);
}
