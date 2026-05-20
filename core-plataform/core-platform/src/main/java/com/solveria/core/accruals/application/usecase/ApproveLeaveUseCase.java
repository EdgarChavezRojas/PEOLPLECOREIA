package com.solveria.core.accruals.application.usecase;

import com.solveria.core.accruals.application.command.ApproveLeaveCommand;
import com.solveria.core.accruals.domain.model.AccrualBalance;

public interface ApproveLeaveUseCase {

  AccrualBalance handle(ApproveLeaveCommand command);
}
