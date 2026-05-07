package com.solveria.core.dossier.application.usecase;

import com.solveria.core.dossier.application.command.AssignAssetCommand;
import com.solveria.core.dossier.domain.model.AssignedAsset;

public interface AssignAssetUseCase {

  AssignedAsset handle(AssignAssetCommand command);
}
