package com.solveria.core.accruals.application.usecase;

import com.solveria.core.accruals.application.command.ProvisionBenefitBatchCommand;
import com.solveria.core.accruals.domain.model.BenefitAccrual;
import java.util.List;

public interface ProvisionBenefitBatchUseCase {

  List<BenefitAccrual> handle(ProvisionBenefitBatchCommand command);
}
