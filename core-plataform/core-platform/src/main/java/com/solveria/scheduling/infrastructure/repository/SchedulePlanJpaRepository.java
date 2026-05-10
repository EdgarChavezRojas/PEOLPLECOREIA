package com.solveria.scheduling.infrastructure.repository;

import com.solveria.scheduling.domain.model.ar.SchedulePlan;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SchedulePlanJpaRepository extends JpaRepository<SchedulePlan, UUID> {
}
