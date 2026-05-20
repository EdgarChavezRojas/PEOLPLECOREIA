package com.solveria.scheduling.infrastructure.repository;

import com.solveria.scheduling.infrastructure.jpa.SchedulePlanJpa;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SchedulePlanJpaRepository extends JpaRepository<SchedulePlanJpa, Long> {

  Optional<SchedulePlanJpa> findByPlanId(UUID planId);
}
