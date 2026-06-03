package com.solveria.core.workforce.infrastructure.repository;

import com.solveria.core.workforce.domain.model.vo.RelationshipStatus;
import com.solveria.core.workforce.domain.model.vo.RelationshipType;
import com.solveria.core.workforce.infrastructure.jpa.RelationshipJpa;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RelationshipRepository extends JpaRepository<RelationshipJpa, UUID> {

  List<RelationshipJpa> findByPersonId(UUID personId);

  List<RelationshipJpa> findByTenantIdAndPersonId(UUID tenantId, UUID personId);

  Optional<RelationshipJpa> findByRelationshipIdAndTenantId(UUID relationshipId, UUID tenantId);

  List<RelationshipJpa> findByTenantId(UUID tenantId);

  boolean existsByPersonIdAndTenantIdAndRelationTypeAndCurrentStatus(
      UUID personId,
      UUID tenantId,
      RelationshipType relationType,
      RelationshipStatus currentStatus);
}
