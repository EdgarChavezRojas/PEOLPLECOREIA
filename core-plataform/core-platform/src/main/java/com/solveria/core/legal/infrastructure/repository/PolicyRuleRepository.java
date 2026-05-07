package com.solveria.core.legal.infrastructure.repository;

import com.solveria.core.legal.infrastructure.jpa.PolicyRuleJpa;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicyRuleRepository extends JpaRepository<PolicyRuleJpa, Long> {

  Optional<PolicyRuleJpa> findByPolicyIdAndTenantId(UUID policyId, String tenantId);
}
