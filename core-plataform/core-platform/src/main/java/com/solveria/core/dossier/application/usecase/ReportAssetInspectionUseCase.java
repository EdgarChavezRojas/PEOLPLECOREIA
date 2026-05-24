package com.solveria.core.dossier.application.usecase;

import com.solveria.core.dossier.application.command.ReportAssetInspectionCommand;
import com.solveria.core.dossier.domain.model.AssignedAsset;

public interface ReportAssetInspectionUseCase {

  AssignedAsset handle(ReportAssetInspectionCommand command);
}
