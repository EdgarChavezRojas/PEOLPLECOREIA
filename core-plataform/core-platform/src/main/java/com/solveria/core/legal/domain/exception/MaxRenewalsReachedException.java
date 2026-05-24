package com.solveria.core.legal.domain.exception;

import com.solveria.core.shared.exceptions.DomainException;
import java.util.Map;

public class MaxRenewalsReachedException extends DomainException {

  public MaxRenewalsReachedException(int renewalCount, int maxRenewals) {
    super(
        "MAX_RENEWALS_REACHED",
        Map.of("renewalCount", renewalCount, "maxRenewals", maxRenewals),
        null);
  }
}
