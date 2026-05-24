package com.solveria.scheduling.application.usecase;

import com.solveria.scheduling.application.port.inbound.RosteringUseCase;
import com.solveria.scheduling.application.port.outbound.SchedulePlanRepositoryPort;
import com.solveria.scheduling.application.port.outbound.SchedulingEventOutboxPort;
import com.solveria.scheduling.domain.event.SchedulePublishedEvent;
import com.solveria.scheduling.domain.model.ar.SchedulePlan;
import com.solveria.scheduling.domain.model.entity.AssignedShift;
import com.solveria.scheduling.domain.service.RosterValidationService;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RosteringUseCaseImpl implements RosteringUseCase {

  private final SchedulePlanRepositoryPort schedulePlanRepositoryPort;
  private final SchedulingEventOutboxPort eventOutboxPort;
  private final RosterValidationService rosterValidationService;

  @Override
  @Transactional
  public void assignShiftToPlan(UUID planId, AssignedShift shift) {
    SchedulePlan plan =
        schedulePlanRepositoryPort
            .findById(planId)
            .orElseThrow(() -> new IllegalArgumentException("SchedulePlan not found"));

    // Extraer turnos existentes del relationshipId para validar políticas
    var existingShifts =
        plan.getShifts().stream()
            .filter(s -> s.getRelationshipId().equals(shift.getRelationshipId()))
            .toList();

    // Validaciones P20 y P24
    rosterValidationService.validateAntiClopening(existingShifts, shift);
    rosterValidationService.validateConsecutiveDaysLimit(existingShifts, shift);

    plan.addShift(shift);
    schedulePlanRepositoryPort.save(plan);
  }

  @Override
  @Transactional
  public void publishPlan(UUID planId) {
    SchedulePlan plan =
        schedulePlanRepositoryPort
            .findById(planId)
            .orElseThrow(() -> new IllegalArgumentException("SchedulePlan not found"));

    plan.publish();
    schedulePlanRepositoryPort.save(plan);

    // Dispara evento asíncrono vía Event Outbox
    eventOutboxPort.publish(new SchedulePublishedEvent(plan.getPlanId(), Instant.now()));
  }
}
