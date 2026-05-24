package com.solveria.core.legal.domain.exception;

import com.solveria.core.shared.exceptions.DomainException;
import java.util.Map;

public class TenantIsolationViolationException extends DomainException {

  public TenantIsolationViolationException(String expectedTenantId, String currentTenantId) {
    super(
        "TENANT_ISOLATION_VIOLATION",
        Map.of("expectedTenantId", expectedTenantId, "currentTenantId", currentTenantId),
        null);
  }
}
