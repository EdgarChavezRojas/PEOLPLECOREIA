package com.solveria.core.accruals.domain.exception;

import com.solveria.core.shared.exceptions.DomainException;
import java.util.Map;

public class InvalidAccrualStateException extends DomainException {

  public InvalidAccrualStateException(String reason) {
    super("INVALID_ACCRUAL_STATE", Map.of("reason", reason), null);
  }
}
