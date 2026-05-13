package com.solveria.core.accruals.application.usecase;

import com.solveria.core.accruals.application.command.RequestQuinquenioPaymentCommand;
import com.solveria.core.accruals.domain.model.QuinquenioProvision;

public interface RequestQuinquenioPaymentUseCase {

  QuinquenioProvision handle(RequestQuinquenioPaymentCommand command);
}

