package com.solveria.core.dossier.infrastructure.repository;

import com.solveria.core.dossier.infrastructure.jpa.TalentInventoryJpa;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TalentInventoryRepository extends JpaRepository<TalentInventoryJpa, UUID> {

  Optional<TalentInventoryJpa> findByRelationshipIdAndTenantId(UUID relationshipId, UUID tenantId);
}
