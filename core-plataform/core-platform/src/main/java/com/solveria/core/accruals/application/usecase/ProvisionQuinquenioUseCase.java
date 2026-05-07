package com.solveria.core.accruals.application.usecase;

import com.solveria.core.accruals.application.command.ProvisionQuinquenioCommand;
import com.solveria.core.accruals.domain.model.QuinquenioProvision;

public interface ProvisionQuinquenioUseCase {

  QuinquenioProvision handle(ProvisionQuinquenioCommand command);
}
