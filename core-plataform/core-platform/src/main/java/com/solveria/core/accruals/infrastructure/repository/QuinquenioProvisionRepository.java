package com.solveria.core.accruals.infrastructure.repository;

import com.solveria.core.accruals.infrastructure.jpa.QuinquenioProvisionJpa;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuinquenioProvisionRepository extends JpaRepository<QuinquenioProvisionJpa, UUID> {

  Optional<QuinquenioProvisionJpa> findByRelationshipIdAndTenantId(
      UUID relationshipId, UUID tenantId);
}
