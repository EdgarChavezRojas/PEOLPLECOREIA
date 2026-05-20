package com.solveria.core.legal.domain.exception;

import com.solveria.core.shared.exceptions.DomainException;
import java.math.BigDecimal;
import java.util.Map;

public class ThresholdNotIncreasedException extends DomainException {

  public ThresholdNotIncreasedException(BigDecimal previousValue, BigDecimal newValue) {
    super(
        "LEGAL_THRESHOLD_NOT_INCREASED",
        Map.of("previousValue", previousValue, "newValue", newValue),
        null);
  }
}
