package com.solveria.core.financial.domain.model;

import com.solveria.core.financial.domain.model.vo.HealthProviderStatus;
import java.util.UUID;

/**
 * Entity: HealthProvider (Caja Nacional de Salud, etc.). Administra la afiliación del trabajador a
 * una caja de salud.
 */
public class HealthProvider {

  private final UUID providerId;
  private final String registrationNo;
  private HealthProviderStatus status;
  private final UUID tenantId;
  private final String createdBy;

  private HealthProvider(
      UUID providerId,
      String registrationNo,
      HealthProviderStatus status,
      UUID tenantId,
      String createdBy) {
    this.providerId = providerId;
    this.registrationNo = registrationNo;
    this.status = status;
    this.tenantId = tenantId;
    this.createdBy = createdBy;
  }

  public static HealthProvider create(String registrationNo, UUID tenantId, String createdBy) {
    if (registrationNo == null || registrationNo.isBlank()) {
      throw new IllegalArgumentException("Número de afiliación es obligatorio");
    }
    return new HealthProvider(
        UUID.randomUUID(), registrationNo, HealthProviderStatus.ACTIVO, tenantId, createdBy);
  }

  public static HealthProvider rehydrate(
      UUID providerId,
      String registrationNo,
      HealthProviderStatus status,
      UUID tenantId,
      String createdBy) {
    return new HealthProvider(providerId, registrationNo, status, tenantId, createdBy);
  }

  public void suspend() {
    this.status = HealthProviderStatus.SUSPENDIDO;
  }

  public void activate() {
    this.status = HealthProviderStatus.ACTIVO;
  }

  public boolean isActive() {
    return this.status == HealthProviderStatus.ACTIVO;
  }

  // --- Getters ---

  public UUID getProviderId() {
    return providerId;
  }

  public String getRegistrationNo() {
    return registrationNo;
  }

  public HealthProviderStatus getStatus() {
    return status;
  }

  public UUID getTenantId() {
    return tenantId;
  }

  public String getCreatedBy() {
    return createdBy;
  }
}
