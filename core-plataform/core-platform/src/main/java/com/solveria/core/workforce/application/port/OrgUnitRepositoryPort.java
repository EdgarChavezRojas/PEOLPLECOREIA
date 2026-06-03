package com.solveria.core.workforce.application.port;

import com.solveria.core.workforce.domain.model.OrgUnit;
import com.solveria.core.workforce.domain.model.OrgUnit.OrgUnitType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Puerto de Salida: Repository Abstracción para OrgUnit
 *
 * <p>Define los contratos que la capa de infraestructura debe implementar. El Adapter encapsula: -
 * Mapeo Domain → JPA - Persistencia - Mapeo JPA → Domain
 *
 * <p>Los Use Cases SOLO conocen esta interfaz, NO conocen JPA ni Mappers.
 */
public interface OrgUnitRepositoryPort {

  /**
   * Guarda una unidad organizativa en el dominio. El adapter internamente: 1. Mapea OrgUnit
   * (dominio) → OrgUnitJpa 2. Persiste en BD 3. Mapea OrgUnitJpa → OrgUnit (dominio) 4. Retorna el
   * dominio guardado
   *
   * @param orgUnit Entidad de dominio
   * @return Entidad de dominio guardada
   */
  OrgUnit save(OrgUnit orgUnit);

  /**
   * Busca por ID y Tenant
   *
   * @param unitId ID de la unidad
   * @param tenantId ID del tenant (para multi-tenant)
   * @return Optional con la entidad de dominio
   */
  Optional<OrgUnit> findByUnitIdAndTenantId(UUID unitId, UUID tenantId);

  /** Verifica si existe una unidad por ID y Tenant */
  boolean existsByUnitIdAndTenantId(UUID unitId, UUID tenantId);

  /** Lista unidades organizativas por tenant con paginacion */
  Page<OrgUnit> findByTenantId(UUID tenantId, Pageable pageable);

  /**
   * Verifica si ya existe una unidad con el mismo nombre y tipo en el tenant (unicidad de negocio)
   */
  boolean existsByNameAndUnitTypeAndTenantId(String name, OrgUnitType unitType, UUID tenantId);
}
