package com.solveria.core.tenantManagement.application.usecase;

import com.solveria.core.tenantManagement.application.dto.TenantResponse;
import com.solveria.core.tenantManagement.application.port.TenantRepositoryPort;
import com.solveria.core.tenantManagement.domain.model.Tenant;
import com.solveria.core.tenantManagement.infrastructure.mapper.TenantMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case: Recuperar todos los tenants del sistema.
 *
 * <p>Orquesta la llamada al puerto TenantRepositoryPort para obtener todos los tenants y mapearlos
 * a DTOs de respuesta.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class GetAllTenantsUseCase {

  private final TenantRepositoryPort tenantRepositoryPort;
  private final TenantMapper tenantMapper;

  /**
   * Ejecuta el caso de uso: recupera todos los tenants y los mapea a TenantResponse.
   *
   * @return Lista de TenantResponse con todos los tenants del sistema
   */
  @Transactional(readOnly = true)
  public List<TenantResponse> execute() {
    log.info("event=TENANT_MANAGEMENT_GET_ALL_TENANTS_START");

    List<Tenant> tenants = tenantRepositoryPort.findAll();

    List<TenantResponse> responses = tenants.stream().map(tenantMapper::toResponse).toList();

    log.info("event=TENANT_MANAGEMENT_GET_ALL_TENANTS_SUCCESS totalTenants={}", responses.size());

    return responses;
  }
}
