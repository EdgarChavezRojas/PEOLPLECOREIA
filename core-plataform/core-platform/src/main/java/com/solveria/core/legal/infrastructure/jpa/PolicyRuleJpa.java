package com.solveria.core.legal.infrastructure.jpa;

import com.solveria.core.shared.base.BaseEntity;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "legal_policy_rule",
    indexes = {
      @Index(name = "idx_legal_policy_rule_policy_id", columnList = "policy_id"),
      @Index(name = "idx_legal_policy_rule_tenant_id", columnList = "tenant_id")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyRuleJpa extends BaseEntity {
  @Id
  @Column(name = "policy_id", nullable = false, unique = true)
  private UUID policyId;

  @Column(name = "policy_name", nullable = false, length = 100)
  private String policyName;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(
      name = "legal_policy_threshold",
      joinColumns = @JoinColumn(name = "policy_id", referencedColumnName = "policy_id"))
  @Builder.Default
  private Set<LegalThresholdEmbeddable> thresholds = new HashSet<>();
}
