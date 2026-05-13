package com.solveria.core.dossier.application.service;

import com.solveria.core.dossier.application.command.AddPerformanceSnapshotCommand;
import com.solveria.core.dossier.application.port.TalentInventoryRepositoryPort;
import com.solveria.core.dossier.application.usecase.AddPerformanceSnapshotUseCase;
import com.solveria.core.dossier.domain.exception.DossierErrorCode;
import com.solveria.core.dossier.domain.exception.TalentInventoryNotFoundException;
import com.solveria.core.dossier.domain.model.PerformanceSnapshot;
import com.solveria.core.dossier.domain.model.TalentInventory;
import com.solveria.core.dossier.domain.policy.LocalizationPolicy;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AddPerformanceSnapshotService implements AddPerformanceSnapshotUseCase {

  private final TalentInventoryRepositoryPort talentInventoryRepository;

  public AddPerformanceSnapshotService(TalentInventoryRepositoryPort talentInventoryRepository) {
    this.talentInventoryRepository = talentInventoryRepository;
  }

  @Override
  public TalentInventory handle(AddPerformanceSnapshotCommand command) {
    if (command == null) {
      throw new IllegalArgumentException(DossierErrorCode.COMMAND_INVALID.name());
    }
    LocalizationPolicy.requireSantaCruz(command.location());
    if (command.relationshipId() == null) {
      throw new IllegalArgumentException(DossierErrorCode.RELATIONSHIP_ID_REQUIRED.name());
    }
    if (command.evalPeriod() == null || command.evalPeriod().isBlank()) {
      throw new IllegalArgumentException(DossierErrorCode.PERFORMANCE_PERIOD_REQUIRED.name());
    }
    if (command.score() == null) {
      throw new IllegalArgumentException(DossierErrorCode.PERFORMANCE_SCORE_REQUIRED.name());
    }
    TalentInventory inventory =
        talentInventoryRepository
            .findByRelationshipId(command.relationshipId())
            .orElseThrow(() -> new TalentInventoryNotFoundException(command.relationshipId()));

    PerformanceSnapshot snapshot =
        new PerformanceSnapshot(UUID.randomUUID(), command.evalPeriod(), command.score());
    inventory.addPerformanceSnapshot(snapshot);
    return talentInventoryRepository.save(inventory);
  }
}


