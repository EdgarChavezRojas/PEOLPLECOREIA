package com.solveria.core.accruals.application.usecase;

import com.solveria.core.accruals.application.command.MarkQuinquenioPaidCommand;
import com.solveria.core.accruals.domain.model.QuinquenioProvision;

public interface MarkQuinquenioPaidUseCase {

  QuinquenioProvision handle(MarkQuinquenioPaidCommand command);
}

