package com.solveria.core.accruals.application.usecase;

import com.solveria.core.accruals.application.command.AccrueVacationCommand;
import com.solveria.core.accruals.domain.model.AccrualBalance;

public interface AccrueVacationUseCase {

  AccrualBalance handle(AccrueVacationCommand command);
}
