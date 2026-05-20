package com.solveria.core.accruals.infrastructure.scheduler;

import com.solveria.core.accruals.application.command.ProvisionBenefitBatchCommand;
import com.solveria.core.accruals.application.usecase.ProvisionBenefitBatchUseCase;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BenefitProvisionScheduler {

  private final ProvisionBenefitBatchUseCase provisionBenefitBatchUseCase;

  public BenefitProvisionScheduler(ProvisionBenefitBatchUseCase provisionBenefitBatchUseCase) {
    this.provisionBenefitBatchUseCase = provisionBenefitBatchUseCase;
  }

  // ocupa su propio configuration para definir a que hora se realiza
  // como el modulo de dossier
  @Scheduled(cron = "0 0 23 L * ?")
  public void runMonthlyBenefitProvision() {
    // TODO: Fetch active relationships from Person bounded context via Port
    List<ProvisionBenefitBatchCommand.BenefitProvisionItem> items = List.of();
    ProvisionBenefitBatchCommand command = new ProvisionBenefitBatchCommand(items, "Santa Cruz");
    provisionBenefitBatchUseCase.handle(command);
  }
}
