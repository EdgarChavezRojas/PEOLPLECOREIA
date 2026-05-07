package com.solveria.core.dossier.application.service;

import com.solveria.core.dossier.application.command.ReturnAssetCommand;
import com.solveria.core.dossier.application.port.AssignedAssetRepositoryPort;
import com.solveria.core.dossier.application.usecase.ReturnAssetUseCase;
import com.solveria.core.dossier.domain.exception.AssignedAssetNotFoundException;
import com.solveria.core.dossier.domain.model.AssignedAsset;
import com.solveria.core.dossier.domain.policy.LocalizationPolicy;

public class ReturnAssetService implements ReturnAssetUseCase {

  private final AssignedAssetRepositoryPort assignedAssetRepository;

  public ReturnAssetService(AssignedAssetRepositoryPort assignedAssetRepository) {
    this.assignedAssetRepository = assignedAssetRepository;
  }

  @Override
  public AssignedAsset handle(ReturnAssetCommand command) {
    LocalizationPolicy.requireSantaCruz(command.location());
    AssignedAsset asset =
        assignedAssetRepository
            .findById(command.assignmentId())
            .orElseThrow(() -> new AssignedAssetNotFoundException(command.assignmentId()));
    if (command.damaged()) {
      asset.reportDamage(command.returnedAt());
    } else {
      asset.returnAsset(command.returnedAt());
    }
    return assignedAssetRepository.save(asset);
  }
}
