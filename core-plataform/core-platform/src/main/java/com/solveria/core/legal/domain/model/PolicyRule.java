package com.solveria.core.legal.domain.model;

import com.solveria.core.legal.domain.exception.TenantIsolationViolationException;
import com.solveria.core.legal.domain.model.vo.LegalThreshold;
import com.solveria.core.security.context.SecurityTenantContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
public class PolicyRule {

  private final UUID policyId;
  private final String policyName;
  private final String description;
  private final String tenantId;
  private final List<LegalThreshold> thresholds;

  public PolicyRule(
      UUID policyId,
      String policyName,
      String description,
      String tenantId,
      List<LegalThreshold> thresholds) {
    this.policyId = Objects.requireNonNull(policyId, "policyId");
    this.policyName = Objects.requireNonNull(policyName, "policyName");
    this.description = Objects.requireNonNull(description, "description");
    this.tenantId = Objects.requireNonNull(tenantId, "tenantId");
    this.thresholds = new ArrayList<>(Objects.requireNonNullElseGet(thresholds, List::of));
    validateTenant();
  }

  public void addThreshold(LegalThreshold threshold) {
    validateTenant();
    thresholds.add(Objects.requireNonNull(threshold, "threshold"));
  }

  private void validateTenant() {
    String currentTenantId = SecurityTenantContext.getCurrentTenantId();
    if (!Objects.equals(tenantId, currentTenantId)) {
      throw new TenantIsolationViolationException(tenantId, currentTenantId);
    }
  }
}
