package com.solveria.core.tenantManagement.infrastructure.mapper;

import com.solveria.core.tenantManagement.application.dto.TenantResponse;
import com.solveria.core.tenantManagement.domain.model.Tenant;
import com.solveria.core.tenantManagement.infrastructure.jpa.TenantJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper para Tenant usando MapStruct.
 *
 * <p>Convierte entre:
 *
 * <ul>
 *   <li>Tenant (Dominio) ↔ TenantJpaEntity (Infraestructura)
 *   <li>Tenant (Dominio) → TenantResponse (Aplicación/Presentación)
 * </ul>
 *
 * <p>El componentModel = "spring" hace que MapStruct genere un bean de Spring automáticamente.
 */
@Mapper(componentModel = "spring")
public interface TenantMapper {

  /**
   * Convierte de Tenant (Dominio) a TenantJpaEntity (Infraestructura).
   *
   * @param tenant Entidad de dominio
   * @return Entidad JPA
   */
  TenantJpaEntity toJpa(Tenant tenant);

  /**
   * Convierte de TenantJpaEntity (Infraestructura) a Tenant (Dominio).
   *
   * @param jpa Entidad JPA
   * @return Entidad de dominio
   */
  Tenant toDomain(TenantJpaEntity jpa);

  /**
   * Convierte de Tenant (Dominio) a TenantResponse (DTO de presentación).
   *
   * @param tenant Entidad de dominio
   * @return DTO de respuesta
   */
  @Mapping(
      target = "status",
      expression = "java(tenant.getStatus() != null ? tenant.getStatus().name() : null)")
  TenantResponse toResponse(Tenant tenant);
}
