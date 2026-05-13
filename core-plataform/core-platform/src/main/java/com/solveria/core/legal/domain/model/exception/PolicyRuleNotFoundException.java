package com.solveria.core.legal.domain.model.exception;

import com.solveria.core.shared.exceptions.DomainException;
import java.util.Map;
import java.util.UUID;

public class PolicyRuleNotFoundException extends DomainException {

  public PolicyRuleNotFoundException(UUID policyId) {
    super("LEGAL_POLICY_RULE_NOT_FOUND", Map.of("policyId", policyId), null);
  }
}

