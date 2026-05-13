package com.solveria.scheduling.infrastructure.adapter;

import com.solveria.scheduling.application.port.outbound.SchedulePlanRepositoryPort;
import com.solveria.scheduling.domain.model.ar.SchedulePlan;
import com.solveria.scheduling.infrastructure.jpa.SchedulePlanJpa;
import com.solveria.scheduling.infrastructure.mapper.SchedulePlanMapper;
import com.solveria.scheduling.infrastructure.repository.SchedulePlanJpaRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SchedulePlanRepositoryAdapter implements SchedulePlanRepositoryPort {

    private final SchedulePlanJpaRepository schedulePlanJpaRepository;
    private final SchedulePlanMapper schedulePlanMapper;

    @Override
    public SchedulePlan save(SchedulePlan plan) {
        SchedulePlanJpa jpa = schedulePlanMapper.toJpa(plan);
        SchedulePlanJpa saved = schedulePlanJpaRepository.save(jpa);
        return schedulePlanMapper.toDomain(saved);
    }

    @Override
    public Optional<SchedulePlan> findById(UUID planId) {
        return schedulePlanJpaRepository.findByPlanId(planId)
            .map(schedulePlanMapper::toDomain);
    }
}
