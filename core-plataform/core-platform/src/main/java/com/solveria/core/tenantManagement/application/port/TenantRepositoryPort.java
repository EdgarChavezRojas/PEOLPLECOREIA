package com.solveria.core.tenantManagement.application.port;

import com.solveria.core.tenantManagement.domain.model.Tenant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de Salida: Repository Abstracción para Tenant
 *
 * <p>Los Use Cases inyectan SOLO esta interfaz, NO TenantJpaRepository de Spring Data. Define los
 * métodos que la capa de Infraestructura debe implementar.
 */
public interface TenantRepositoryPort {

  /**
   * Guarda un tenant en el dominio. El adapter internamente maneja: mapeo, persistencia, mapeo de
   * vuelta.
   *
   * @param tenant Entidad de dominio
   * @return Entidad de dominio guardada
   */
  Tenant save(Tenant tenant);

  /**
   * Busca un tenant por su ID.
   *
   * @param tenantId ID único del tenant
   * @return Optional con el tenant si existe
   */
  Optional<Tenant> findById(UUID tenantId);

  /**
   * Recupera todos los tenants del sistema.
   *
   * @return Lista de todos los tenants
   */
  List<Tenant> findAll();

  /**
   * Busca un tenant por nombre.
   *
   * @param name Nombre del tenant
   * @return Optional con el tenant si existe
   */
  Optional<Tenant> findByName(String name);

  /**
   * Verifica si existe un tenant con el ID especificado.
   *
   * @param tenantId ID a verificar
   * @return true si existe, false en caso contrario
   */
  boolean existsById(UUID tenantId);

  /**
   * Elimina un tenant por ID.
   *
   * @param tenantId ID del tenant a eliminar
   */
  void deleteById(UUID tenantId);

  /**
   * Cuenta el total de tenants en el sistema.
   *
   * @return Cantidad total de tenants
   */
  long count();
}
