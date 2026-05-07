package com.solveria.core.experience.infrastructure.repository;

import com.solveria.core.experience.infrastructure.jpa.SelfServiceActionJpa;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SelfServiceActionRepository extends JpaRepository<SelfServiceActionJpa, UUID> {

  Optional<SelfServiceActionJpa> findByActionIdAndTenantId(UUID actionId, String tenantId);

  List<SelfServiceActionJpa> findByPersonIdAndTenantId(UUID personId, String tenantId);
}
