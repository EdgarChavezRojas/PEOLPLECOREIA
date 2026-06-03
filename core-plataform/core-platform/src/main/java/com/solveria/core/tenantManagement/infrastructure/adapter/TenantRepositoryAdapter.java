package com.solveria.core.tenantManagement.infrastructure.adapter;

import com.solveria.core.tenantManagement.application.port.TenantRepositoryPort;
import com.solveria.core.tenantManagement.domain.model.Tenant;
import com.solveria.core.tenantManagement.infrastructure.jpa.TenantJpaEntity;
import com.solveria.core.tenantManagement.infrastructure.mapper.TenantMapper;
import com.solveria.core.tenantManagement.infrastructure.repository.TenantJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adaptador que implementa el puerto TenantRepositoryPort.
 *
 * <p>Orquesta la interacción entre:
 *
 * <ul>
 *   <li>La capa de Aplicación (que inyecta el puerto)
 *   <li>La capa de Infraestructura (TenantJpaRepository y TenantMapper)
 * </ul>
 *
 * <p>Responsabilidades:
 *
 * <ul>
 *   <li>Mapear Tenant (Dominio) ↔ TenantJpaEntity (Infraestructura)
 *   <li>Gestionar transacciones
 *   <li>Ejecutar operaciones persistentes
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantRepositoryAdapter implements TenantRepositoryPort {

  private final TenantJpaRepository tenantJpaRepository;
  private final TenantMapper tenantMapper;

  /**
   * Guarda un tenant en la base de datos.
   *
   * @param tenant Entidad de dominio a guardar
   * @return Entidad de dominio guardada
   */
  @Override
  @Transactional
  public Tenant save(Tenant tenant) {
    log.debug("Guardando tenant con ID: {}", tenant.getTenantId());
    TenantJpaEntity tenantJpa = tenantMapper.toJpa(tenant);
    TenantJpaEntity savedTenantJpa = tenantJpaRepository.save(tenantJpa);
    Tenant savedTenant = tenantMapper.toDomain(savedTenantJpa);
    log.debug("Tenant guardado exitosamente con ID: {}", savedTenant.getTenantId());
    return savedTenant;
  }

  /**
   * Busca un tenant por ID.
   *
   * @param tenantId ID del tenant
   * @return Optional con el tenant si existe
   */
  @Override
  @Transactional(readOnly = true)
  public Optional<Tenant> findById(UUID tenantId) {
    log.debug("Buscando tenant con ID: {}", tenantId);
    return tenantJpaRepository.findById(tenantId).map(tenantMapper::toDomain);
  }

  /**
   * Recupera todos los tenants del sistema.
   *
   * @return Lista de todos los tenants
   */
  @Override
  @Transactional(readOnly = true)
  public List<Tenant> findAll() {
    log.debug("Recuperando todos los tenants");
    List<TenantJpaEntity> tenantJpaEntities = tenantJpaRepository.findAll();
    return tenantJpaEntities.stream().map(tenantMapper::toDomain).toList();
  }

  /**
   * Busca un tenant por nombre.
   *
   * @param name Nombre del tenant
   * @return Optional con el tenant si existe
   */
  @Override
  @Transactional(readOnly = true)
  public Optional<Tenant> findByName(String name) {
    log.debug("Buscando tenant con nombre: {}", name);
    return tenantJpaRepository.findByName(name).map(tenantMapper::toDomain);
  }

  /**
   * Verifica si existe un tenant con el ID especificado.
   *
   * @param tenantId ID a verificar
   * @return true si existe, false en caso contrario
   */
  @Override
  @Transactional(readOnly = true)
  public boolean existsById(UUID tenantId) {
    log.debug("Verificando existencia de tenant con ID: {}", tenantId);
    return tenantJpaRepository.existsById(tenantId);
  }

  /**
   * Elimina un tenant por ID.
   *
   * @param tenantId ID del tenant a eliminar
   */
  @Override
  @Transactional
  public void deleteById(UUID tenantId) {
    log.debug("Eliminando tenant con ID: {}", tenantId);
    tenantJpaRepository.deleteById(tenantId);
    log.debug("Tenant eliminado exitosamente con ID: {}", tenantId);
  }

  /**
   * Cuenta el total de tenants en el sistema.
   *
   * @return Cantidad total de tenants
   */
  @Override
  @Transactional(readOnly = true)
  public long count() {
    log.debug("Contando total de tenants");
    return tenantJpaRepository.count();
  }
}
