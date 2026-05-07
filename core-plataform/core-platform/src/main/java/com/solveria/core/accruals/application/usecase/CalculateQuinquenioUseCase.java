package com.solveria.core.accruals.application.usecase;

import com.solveria.core.accruals.application.command.CalculateQuinquenioCommand;
import com.solveria.core.accruals.domain.model.QuinquenioProvision;

public interface CalculateQuinquenioUseCase {

  QuinquenioProvision handle(CalculateQuinquenioCommand command);
}
