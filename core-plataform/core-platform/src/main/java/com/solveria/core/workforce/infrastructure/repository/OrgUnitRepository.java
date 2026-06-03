package com.solveria.core.workforce.infrastructure.repository;

import com.solveria.core.workforce.domain.model.OrgUnit.OrgUnitType;
import com.solveria.core.workforce.infrastructure.jpa.OrgUnitJpa;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrgUnitRepository extends JpaRepository<OrgUnitJpa, UUID> {

  List<OrgUnitJpa> findByParentId(UUID parentId);

  List<OrgUnitJpa> findByTenantIdAndIsRootTrue(UUID tenantId);

  @EntityGraph(attributePaths = {"orgHierarchies"})
  List<OrgUnitJpa> findByTenantId(UUID tenantId);

  /**
   * Busca una OrgUnit validando que pertenezca al tenant correcto. Crítico para evitar escalada de
   * privilegios entre tenants.
   */
  @EntityGraph(attributePaths = {"orgHierarchies"})
  Optional<OrgUnitJpa> findByUnitIdAndTenantId(UUID unitId, UUID tenantId);

  /** Verifica unicidad de nombre+tipo dentro de un tenant para evitar duplicados de negocio */
  boolean existsByNameAndUnitTypeAndTenantId(String name, OrgUnitType unitType, UUID tenantId);
}
