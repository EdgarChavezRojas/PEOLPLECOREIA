package com.solveria.core.accruals.application.usecase;

import com.solveria.core.accruals.application.command.EvaluateQuinquenioPenaltyCommand;
import com.solveria.core.accruals.domain.model.QuinquenioProvision;

public interface EvaluateQuinquenioPenaltyUseCase {

  QuinquenioProvision handle(EvaluateQuinquenioPenaltyCommand command);
}

