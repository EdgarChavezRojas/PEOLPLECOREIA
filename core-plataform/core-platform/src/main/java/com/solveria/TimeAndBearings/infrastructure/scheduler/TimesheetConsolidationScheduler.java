package com.solveria.TimeAndBearings.infrastructure.scheduler;

import com.solveria.TimeAndBearings.application.usecase.TimesheetConsolidationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TimesheetConsolidationScheduler {

  private final TimesheetConsolidationUseCase useCase;

  @Scheduled(cron = "${timesheet.cron.grace-period-closure}")
  public void runGracePeriodClosure() {
    useCase.evaluateAndExecuteGracePeriodClosure();
  }

  @Scheduled(cron = "${timesheet.cron.nightly-consolidation}")
  public void runNightlyConsolidation() {
    useCase.runNightlyConsolidationCron();
  }
}
