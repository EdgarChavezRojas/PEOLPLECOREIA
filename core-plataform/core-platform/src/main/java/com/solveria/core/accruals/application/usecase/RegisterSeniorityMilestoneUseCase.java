package com.solveria.core.accruals.application.usecase;

import com.solveria.core.accruals.application.command.RegisterSeniorityMilestoneCommand;
import com.solveria.core.accruals.domain.model.AccrualBalance;

public interface RegisterSeniorityMilestoneUseCase {

  AccrualBalance handle(RegisterSeniorityMilestoneCommand command);
}
