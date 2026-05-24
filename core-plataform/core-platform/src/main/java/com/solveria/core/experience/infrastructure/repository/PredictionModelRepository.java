package com.solveria.core.experience.infrastructure.repository;

import com.solveria.core.experience.infrastructure.jpa.PredictionModelJpa;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PredictionModelRepository extends JpaRepository<PredictionModelJpa, UUID> {

  Optional<PredictionModelJpa> findByModelIdAndTenantId(UUID modelId, UUID tenantId);
}
