package com.solveria.core.dossier.application.service;

import com.solveria.core.dossier.application.command.AssignAssetCommand;
import com.solveria.core.dossier.application.port.AssignedAssetRepositoryPort;
import com.solveria.core.dossier.application.usecase.AssignAssetUseCase;
import com.solveria.core.dossier.domain.model.AssignedAsset;
import com.solveria.core.dossier.domain.policy.LocalizationPolicy;
import com.solveria.core.security.context.SecurityTenantContext;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AssignAssetService implements AssignAssetUseCase {

  private final AssignedAssetRepositoryPort assignedAssetRepository;

  public AssignAssetService(AssignedAssetRepositoryPort assignedAssetRepository) {
    this.assignedAssetRepository = assignedAssetRepository;
  }

  @Override
  public AssignedAsset handle(AssignAssetCommand command) {
    LocalizationPolicy.requireSantaCruz(command.location());
    AssignedAsset asset =
        AssignedAsset.assign(
            command.workerId(),
            command.assetTag(),
            command.descriptor(),
            command.assignedAt(),
            UUID.fromString(SecurityTenantContext.getCurrentTenantId()));
    if (command.currentLocation() != null
        && command.targetLocation() != null
        && !command.currentLocation().equalsIgnoreCase(command.targetLocation())) {
      asset.requestTransfer();
    }
    return assignedAssetRepository.save(asset);
  }
}
