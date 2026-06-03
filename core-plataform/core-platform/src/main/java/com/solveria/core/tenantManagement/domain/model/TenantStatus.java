package com.solveria.core.tenantManagement.domain.model;

/** Estados posibles para un Tenant. */
public enum TenantStatus {
  ACTIVE,
  INACTIVE;

  public static TenantStatus fromString(String value) {
    if (value == null) return ACTIVE;
    try {
      return TenantStatus.valueOf(value.trim().toUpperCase());
    } catch (IllegalArgumentException ex) {
      return ACTIVE;
    }
  }

  @Override
  public String toString() {
    return name();
  }
}
