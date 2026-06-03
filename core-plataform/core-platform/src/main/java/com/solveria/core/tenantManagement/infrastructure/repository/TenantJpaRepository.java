package com.solveria.core.tenantManagement.infrastructure.repository;

import com.solveria.core.tenantManagement.infrastructure.jpa.TenantJpaEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Interfaz Spring Data JPA Repository para TenantJpaEntity.
 *
 * <p>Proporciona operaciones CRUD básicas y métodos de consulta personalizados contra la base de
 * datos.
 *
 * <p>Esta interfaz NO se inyecta directamente en Use Cases. Se inyecta en el Adapter
 * (TenantRepositoryAdapter) que implementa el puerto de salida.
 */
@Repository
public interface TenantJpaRepository extends JpaRepository<TenantJpaEntity, UUID> {

  /**
   * Busca un tenant por nombre.
   *
   * @param name Nombre del tenant
   * @return Optional con el tenant si existe
   */
  Optional<TenantJpaEntity> findByName(String name);
}
