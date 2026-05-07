package com.solveria.core.legal.domain.exception;

import com.solveria.core.shared.exceptions.DomainException;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

public class EffectiveDatingOverlapException extends DomainException {

  public EffectiveDatingOverlapException(
      UUID existingAddendumId, LocalDate effectiveFrom, LocalDate effectiveTo) {
    super(
        "CONTRACT_ADDENDUM_EFFECTIVE_DATING_OVERLAP",
        Map.of(
            "existingAddendumId", existingAddendumId,
            "effectiveFrom", effectiveFrom,
            "effectiveTo", effectiveTo),
        null);
  }
}
