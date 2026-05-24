package com.solveria.core.workforce.infrastructure.repository;

import com.solveria.core.workforce.infrastructure.jpa.OrgUnitJpa;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrgUnitRepository extends JpaRepository<OrgUnitJpa, UUID> {

  List<OrgUnitJpa> findByParentId(UUID parentId);

  List<OrgUnitJpa> findByTenantIdAndIsRootTrue(UUID tenantId);

  List<OrgUnitJpa> findByTenantId(UUID tenantId);

  /**
   * Busca una OrgUnit validando que pertenezca al tenant correcto. Crítico para evitar escalada de
   * privilegios entre tenants.
   */
  Optional<OrgUnitJpa> findByUnitIdAndTenantId(UUID unitId, UUID tenantId);
}
