package com.solveria.core.workforce.infrastructure.repository;

import com.solveria.core.workforce.infrastructure.jpa.AcademicProfileJpa;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AcademicProfileRepository extends JpaRepository<AcademicProfileJpa, UUID> {

  Optional<AcademicProfileJpa> findByRelationshipId(UUID relationshipId);
}
