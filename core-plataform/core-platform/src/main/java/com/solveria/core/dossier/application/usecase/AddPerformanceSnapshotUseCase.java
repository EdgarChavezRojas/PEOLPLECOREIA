package com.solveria.core.dossier.application.usecase;

import com.solveria.core.dossier.application.command.AddPerformanceSnapshotCommand;
import com.solveria.core.dossier.domain.model.TalentInventory;

public interface AddPerformanceSnapshotUseCase {

  TalentInventory handle(AddPerformanceSnapshotCommand command);
}
