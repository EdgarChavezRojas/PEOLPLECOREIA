package com.solveria.scheduling.infrastructure.adapter;

import com.solveria.scheduling.application.port.outbound.SchedulePlanRepositoryPort;
import com.solveria.scheduling.domain.model.ar.SchedulePlan;
import com.solveria.scheduling.infrastructure.repository.SchedulePlanJpaRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SchedulePlanRepositoryAdapter implements SchedulePlanRepositoryPort {

    private final SchedulePlanJpaRepository schedulePlanJpaRepository;

    @Override
    public SchedulePlan save(SchedulePlan plan) {
        return schedulePlanJpaRepository.save(plan);
    }

    @Override
    public Optional<SchedulePlan> findById(UUID planId) {
        return schedulePlanJpaRepository.findById(planId);
    }
}
