package com.solveria.core.accruals.domain.exception;

import com.solveria.core.shared.exceptions.DomainException;
import java.util.Map;
import java.util.UUID;

public class AccrualBalanceNotFoundException extends DomainException {

  public AccrualBalanceNotFoundException(UUID balanceId) {
    super("ACCRUAL_BALANCE_NOT_FOUND", Map.of("balanceId", balanceId), null);
  }
}
