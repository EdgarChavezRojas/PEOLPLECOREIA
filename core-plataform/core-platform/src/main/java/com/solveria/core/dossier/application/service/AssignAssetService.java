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
    UUID tenantId = UUID.fromString(SecurityTenantContext.getCurrentTenantId());

    // Unicidad de negocio: evita asignar el mismo activo (assetTag) dos veces al mismo colaborador
    if (assignedAssetRepository.existsActiveAssignmentForWorkerAndTag(
        command.workerId(), command.assetTag(), tenantId)) {
      throw new com.solveria.core.shared.exceptions.BusinessRuleViolationException(
          "El activo '"
              + command.assetTag()
              + "' ya está asignado activamente al colaborador "
              + command.workerId());
    }

    AssignedAsset asset =
        AssignedAsset.assign(
            command.workerId(),
            command.assetTag(),
            command.descriptor(),
            command.assignedAt(),
            tenantId);
    if (command.currentLocation() != null
        && command.targetLocation() != null
        && !command.currentLocation().equalsIgnoreCase(command.targetLocation())) {
      asset.requestTransfer();
    }
    return assignedAssetRepository.save(asset);
  }
}
