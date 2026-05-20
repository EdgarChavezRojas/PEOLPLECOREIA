package com.solveria.core.accruals.application.usecase;

import com.solveria.core.accruals.application.command.OpenAccrualBalanceCommand;
import com.solveria.core.accruals.domain.model.AccrualBalance;

public interface OpenAccrualBalanceUseCase {

  AccrualBalance handle(OpenAccrualBalanceCommand command);
}
