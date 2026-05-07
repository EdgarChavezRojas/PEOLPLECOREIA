package com.solveria.core.legal.domain.exception;

import com.solveria.core.shared.exceptions.DomainException;
import java.util.Map;

public class SegregationOfDutiesViolationException extends DomainException {

  public SegregationOfDutiesViolationException(String createdBy, String approvedBy) {
    super(
        "SEGREGATION_OF_DUTIES_VIOLATION",
        Map.of("createdBy", createdBy, "approvedBy", approvedBy),
        null);
  }
}
