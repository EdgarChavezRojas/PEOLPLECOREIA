package com.solveria.core.tenantManagement.domain.model;

import com.solveria.core.shared.outbox.domain.DomainRoot;
import java.time.Instant;
import java.util.UUID;

public class Tenant extends DomainRoot {

  private UUID tenantId;
  private String name;
  private TenantStatus status;
  private String description;
  private Instant createdAt;
  private Instant updatedAt;

  public Tenant() {}

  public Tenant(
      UUID tenantId,
      String name,
      TenantStatus status,
      String description,
      Instant createdAt,
      Instant updatedAt) {
    this.tenantId = tenantId;
    this.name = name;
    this.status = status;
    this.description = description;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static Tenant create(String name, String description) {
    return new Tenant(
        UUID.randomUUID(), name, TenantStatus.ACTIVE, description, Instant.now(), Instant.now());
  }

  public void updateName(String newName) {
    this.name = newName;
    this.updatedAt = Instant.now();
  }

  public void updateDescription(String newDescription) {
    this.description = newDescription;
    this.updatedAt = Instant.now();
  }

  public void changeStatus(TenantStatus newStatus) {
    this.status = newStatus;
    this.updatedAt = Instant.now();
  }

  public void deactivate() {
    this.status = TenantStatus.INACTIVE;
    this.updatedAt = Instant.now();
  }

  public void activate() {
    this.status = TenantStatus.ACTIVE;
    this.updatedAt = Instant.now();
  }

  public boolean isActive() {
    return status == TenantStatus.ACTIVE;
  }

  public UUID getTenantId() {
    return tenantId;
  }

  public void setTenantId(UUID tenantId) {
    this.tenantId = tenantId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public TenantStatus getStatus() {
    return status;
  }

  public void setStatus(TenantStatus status) {
    this.status = status;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }
}
