package com.solveria.core.accruals.application.usecase;

import com.solveria.core.accruals.application.command.RejectLeaveCommand;
import com.solveria.core.accruals.domain.model.AccrualBalance;

public interface RejectLeaveUseCase {

  AccrualBalance handle(RejectLeaveCommand command);
}
