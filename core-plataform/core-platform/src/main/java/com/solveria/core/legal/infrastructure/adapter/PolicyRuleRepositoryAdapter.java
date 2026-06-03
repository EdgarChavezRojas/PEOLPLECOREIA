package com.solveria.core.legal.infrastructure.adapter;

import com.solveria.core.legal.application.port.PolicyRuleRepositoryPort;
import com.solveria.core.legal.domain.model.PolicyRule;
import com.solveria.core.legal.infrastructure.jpa.PolicyRuleJpa;
import com.solveria.core.legal.infrastructure.mapper.PolicyRuleMapper;
import com.solveria.core.legal.infrastructure.repository.PolicyRuleRepository;
import com.solveria.core.security.context.SecurityTenantContext;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PolicyRuleRepositoryAdapter implements PolicyRuleRepositoryPort {

  private final PolicyRuleRepository policyRuleRepository;
  private final PolicyRuleMapper policyRuleMapper;

  @Override
  @Transactional
  public void save(PolicyRule policyRule) {
    PolicyRuleJpa jpa =
        policyRuleRepository
            .findByPolicyId(policyRule.getPolicyId())
            .orElseGet(
                () -> {
                  PolicyRuleJpa newJpa = new PolicyRuleJpa();
                  newJpa.setPolicyId(policyRule.getPolicyId());
                  return newJpa;
                });
    policyRuleMapper.updateJpaFromDomain(policyRule, jpa);
    policyRuleRepository.save(jpa);
  }

  @Override
  public Optional<PolicyRule> findById(UUID policyId) {
    String tenantStr = SecurityTenantContext.getCurrentTenantId();
    if (tenantStr == null || tenantStr.isBlank()) {
      return policyRuleRepository.findByPolicyId(policyId).map(policyRuleMapper::toDomain);
    }
    UUID currentTenantId = UUID.fromString(tenantStr);
    return policyRuleRepository
        .findByPolicyIdAndTenantId(policyId, currentTenantId)
        .map(policyRuleMapper::toDomain);
  }

  @Override
  public Optional<PolicyRule> findByPolicyName(String policyName) {
    String tenantStr = SecurityTenantContext.getCurrentTenantId();
    if (tenantStr == null || tenantStr.isBlank()) {
      return policyRuleRepository.findByPolicyName(policyName).map(policyRuleMapper::toDomain);
    }
    UUID currentTenantId = UUID.fromString(tenantStr);
    return policyRuleRepository
        .findByPolicyNameAndTenantId(policyName, currentTenantId)
        .map(policyRuleMapper::toDomain);
  }
}
