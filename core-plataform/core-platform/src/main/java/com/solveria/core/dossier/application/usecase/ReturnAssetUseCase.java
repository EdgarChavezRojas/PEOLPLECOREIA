package com.solveria.core.dossier.application.usecase;

import com.solveria.core.dossier.application.command.ReturnAssetCommand;
import com.solveria.core.dossier.domain.model.AssignedAsset;

public interface ReturnAssetUseCase {

  AssignedAsset handle(ReturnAssetCommand command);
}
