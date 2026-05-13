package com.solveria.core.legal.domain.exception;

import com.solveria.core.shared.exceptions.DomainException;
import java.util.Map;

public class TenantMismatchException extends DomainException {

  public TenantMismatchException(String requestedTenantId, String currentTenantId) {
    super(
        "LEGAL_TENANT_MISMATCH",
        Map.of("requestedTenantId", requestedTenantId, "currentTenantId", currentTenantId),
        null);
  }
}

