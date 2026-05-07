package com.solveria.core.accruals.domain.exception;

import com.solveria.core.shared.exceptions.DomainException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public class InsufficientAccrualBalanceException extends DomainException {

  public InsufficientAccrualBalanceException(
      UUID balanceId, BigDecimal requested, BigDecimal available) {
    super(
        "INSUFFICIENT_ACCRUAL_BALANCE",
        Map.of("balanceId", balanceId, "requested", requested, "available", available),
        null);
  }
}
