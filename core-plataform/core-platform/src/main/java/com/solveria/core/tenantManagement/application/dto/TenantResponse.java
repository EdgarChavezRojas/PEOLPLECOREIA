package com.solveria.core.tenantManagement.application.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para Tenant.
 *
 * <p>Se utiliza para transferir datos de Tenant hacia la capa de presentación (REST API).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantResponse {

  private UUID tenantId;
  private String name;
  private String status;
  private String description;
}
