package com.solveria.core.legal.application.port;

import com.solveria.core.legal.domain.model.PolicyRule;
import java.util.Optional;
import java.util.UUID;

public interface PolicyRuleRepositoryPort {

  Optional<PolicyRule> findById(UUID policyId);

  Optional<PolicyRule> findByPolicyName(String policyName);

  void save(PolicyRule policyRule);
}
