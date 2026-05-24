package com.solveria.core.accruals.application.usecase;

import com.solveria.core.accruals.application.command.RequestLeaveCommand;
import com.solveria.core.accruals.domain.model.AccrualBalance;

public interface RequestLeaveUseCase {

  AccrualBalance handle(RequestLeaveCommand command);
}
