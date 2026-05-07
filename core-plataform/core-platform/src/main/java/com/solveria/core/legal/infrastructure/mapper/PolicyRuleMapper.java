package com.solveria.core.legal.infrastructure.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solveria.core.legal.domain.model.PolicyRule;
import com.solveria.core.legal.domain.model.vo.LegalThreshold;
import com.solveria.core.legal.infrastructure.jpa.LegalThresholdEmbeddable;
import com.solveria.core.legal.infrastructure.jpa.PolicyRuleJpa;
import java.util.List;
import java.util.Map;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PolicyRuleMapper {

  PolicyRuleJpa toJpa(PolicyRule policyRule);

  LegalThresholdEmbeddable toEmbeddable(LegalThreshold threshold);

  default PolicyRule toDomain(PolicyRuleJpa jpa) {
    if (jpa == null) {
      return null;
    }
    List<LegalThreshold> thresholds =
        jpa.getThresholds() == null
            ? List.of()
            : jpa.getThresholds().stream().map(this::toDomain).toList();
    return new PolicyRule(
        jpa.getPolicyId(),
        jpa.getPolicyName(),
        jpa.getDescription(),
        jpa.getTenantId(),
        thresholds);
  }

  default LegalThreshold toDomain(LegalThresholdEmbeddable embeddable) {
    if (embeddable == null) {
      return null;
    }
    return new LegalThreshold(embeddable.getThresholdValue(), embeddable.getEffectiveDate());
  }

  @AfterMapping
  default void setTenant(@MappingTarget PolicyRuleJpa policyRuleJpa, PolicyRule policyRule) {
    if (policyRuleJpa != null) {
      policyRuleJpa.setTenantId(policyRule.getTenantId());
    }
  }

  default String toEventPayload(PolicyRule policyRule) {
    if (policyRule == null) {
      return "{}";
    }
    Map<String, Object> payload =
        Map.of(
            "policyId", policyRule.getPolicyId(),
            "policyName", policyRule.getPolicyName(),
            "tenantId", policyRule.getTenantId());
    try {
      return new ObjectMapper().writeValueAsString(payload);
    } catch (Exception e) {
      throw new RuntimeException("Error serializando PolicyRule a JSON", e);
    }
  }
}
