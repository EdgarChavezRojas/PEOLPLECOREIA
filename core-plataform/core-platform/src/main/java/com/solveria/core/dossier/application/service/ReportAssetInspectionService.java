package com.solveria.core.dossier.application.service;

import com.solveria.core.dossier.application.command.ReportAssetInspectionCommand;
import com.solveria.core.dossier.application.port.AssignedAssetRepositoryPort;
import com.solveria.core.dossier.application.usecase.ReportAssetInspectionUseCase;
import com.solveria.core.dossier.domain.exception.AssignedAssetNotFoundException;
import com.solveria.core.dossier.domain.exception.DossierErrorCode;
import com.solveria.core.dossier.domain.model.AssignedAsset;
import com.solveria.core.dossier.domain.policy.LocalizationPolicy;
import org.springframework.stereotype.Service;

@Service
public class ReportAssetInspectionService implements ReportAssetInspectionUseCase {

  private final AssignedAssetRepositoryPort assignedAssetRepository;

  public ReportAssetInspectionService(AssignedAssetRepositoryPort assignedAssetRepository) {
    this.assignedAssetRepository = assignedAssetRepository;
  }

  @Override
  public AssignedAsset handle(ReportAssetInspectionCommand command) {
    if (command == null) {
      throw new IllegalArgumentException(DossierErrorCode.COMMAND_INVALID.name());
    }
    LocalizationPolicy.requireSantaCruz(command.location());
    if (command.assignmentId() == null) {
      throw new IllegalArgumentException(DossierErrorCode.ASSIGNMENT_ID_REQUIRED.name());
    }
    if (command.descriptor() == null) {
      throw new IllegalArgumentException(DossierErrorCode.ASSET_DESCRIPTOR_REQUIRED.name());
    }
    AssignedAsset asset =
        assignedAssetRepository
            .findById(command.assignmentId())
            .orElseThrow(() -> new AssignedAssetNotFoundException(command.assignmentId()));

    asset.reportInspection(command.descriptor(), command.minorDamageReported());
    return assignedAssetRepository.save(asset);
  }
}
