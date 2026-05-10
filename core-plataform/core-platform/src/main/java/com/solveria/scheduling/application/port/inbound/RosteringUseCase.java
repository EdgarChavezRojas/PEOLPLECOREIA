package com.solveria.scheduling.application.port.inbound;

import com.solveria.scheduling.domain.model.entity.AssignedShift;
import java.util.UUID;

public interface RosteringUseCase {
    void assignShiftToPlan(UUID planId, AssignedShift shift);
    void publishPlan(UUID planId);
}
