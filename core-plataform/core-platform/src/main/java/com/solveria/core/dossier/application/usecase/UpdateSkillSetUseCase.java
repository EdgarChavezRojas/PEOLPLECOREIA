package com.solveria.core.dossier.application.usecase;

import com.solveria.core.dossier.application.command.UpdateSkillSetCommand;
import com.solveria.core.dossier.domain.model.TalentInventory;

public interface UpdateSkillSetUseCase {

  TalentInventory handle(UpdateSkillSetCommand command);
}

