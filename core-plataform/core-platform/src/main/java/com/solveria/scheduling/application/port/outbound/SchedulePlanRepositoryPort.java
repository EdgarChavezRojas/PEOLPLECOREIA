package com.solveria.scheduling.application.port.outbound;

import com.solveria.scheduling.domain.model.ar.SchedulePlan;
import java.util.Optional;
import java.util.UUID;

public interface SchedulePlanRepositoryPort {
  SchedulePlan save(SchedulePlan plan);

  Optional<SchedulePlan> findById(UUID planId);
}
