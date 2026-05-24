package com.solveria.core.dossier.application.usecase;

import com.solveria.core.dossier.application.command.UpdateAcademicRankCommand;
import com.solveria.core.dossier.domain.model.TalentInventory;

public interface UpdateAcademicRankUseCase {

  TalentInventory handle(UpdateAcademicRankCommand command);
}
