package com.solveria.core.workforce.infrastructure.repository;

import com.solveria.core.workforce.infrastructure.jpa.WorkerProfileJpa;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkerProfileRepository extends JpaRepository<WorkerProfileJpa, UUID> {

  Optional<WorkerProfileJpa> findByRelationshipId(UUID relationshipId);
}
