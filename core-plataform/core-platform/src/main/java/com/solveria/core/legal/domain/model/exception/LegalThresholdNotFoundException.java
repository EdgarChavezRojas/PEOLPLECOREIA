package com.solveria.core.legal.domain.model.exception;

import com.solveria.core.shared.exceptions.DomainException;
import java.util.Map;
import java.util.UUID;

public class LegalThresholdNotFoundException extends DomainException {

  public LegalThresholdNotFoundException(UUID policyId) {
    super("LEGAL_POLICY_RULE_NO_THRESHOLD", Map.of("policyId", policyId), null);
  }
}

