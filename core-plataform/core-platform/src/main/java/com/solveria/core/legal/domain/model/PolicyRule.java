package com.solveria.core.legal.domain.model;

import com.solveria.core.legal.domain.model.vo.LegalThreshold;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class PolicyRule {

  private final UUID policyId;
  private final String policyName;
  private final String description;
  private final UUID tenantId;
  private final List<LegalThreshold> thresholds;

  public UUID getPolicyId() {
    return policyId;
  }

  public String getPolicyName() {
    return policyName;
  }

  private String getDescription() {
    return description;
  }

  public UUID getTenantId() {
    return tenantId;
  }

  public List<LegalThreshold> getThresholds() {
    return new ArrayList<>(thresholds);
  }

  public PolicyRule(
      UUID policyId,
      String policyName,
      String description,
      UUID tenantId,
      List<LegalThreshold> thresholds) {
    this.policyId = Objects.requireNonNull(policyId, "policyId");
    this.policyName = Objects.requireNonNull(policyName, "policyName");
    this.description = Objects.requireNonNull(description, "description");
    this.tenantId = Objects.requireNonNull(tenantId, "tenantId");
    this.thresholds = new ArrayList<>(Objects.requireNonNullElseGet(thresholds, List::of));
  }

  public void addThreshold(LegalThreshold threshold) {

    thresholds.add(Objects.requireNonNull(threshold, "threshold"));
  }
}
