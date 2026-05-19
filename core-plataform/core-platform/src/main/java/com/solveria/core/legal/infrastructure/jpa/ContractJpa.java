package com.solveria.core.legal.infrastructure.jpa;

import com.solveria.core.legal.domain.model.vo.ContractStatus;
import com.solveria.core.legal.domain.model.vo.ContractType;
import com.solveria.core.legal.domain.model.vo.EmploymentCondition;
import com.solveria.core.shared.base.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(
    name = "legal_contract",
    indexes = {
      @Index(name = "idx_legal_contract_contract_id", columnList = "contract_id"),
      @Index(name = "idx_legal_contract_tenant_id", columnList = "tenant_id"),
      @Index(name = "idx_legal_contract_status", columnList = "status")
    })
@Getter
@Setter
@ToString(exclude = "addendums")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractJpa extends BaseEntity {

  @Column(name = "contract_id", nullable = false, unique = true)
  private UUID contractId;

  @Column(name = "relationship_id", nullable = false)
  private UUID relationshipId;

  @Enumerated(EnumType.STRING)
  @Column(name = "contract_type", nullable = false, length = 20)
  private ContractType contractType;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private ContractStatus status;

  @Column(name = "project_id", length = 50)
  private UUID projectId;

  @Enumerated(EnumType.STRING)
  @Column(name = "employment_cond", length = 2)
  private EmploymentCondition employmentCond;

  @Column(name = "tacita_reconduccion_alert_sent", nullable = false)
  private boolean tacitaReconduccionAlertSent;

  @OneToMany(
      mappedBy = "contract",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @Builder.Default
  private List<ContractAddendumJpa> addendums = new ArrayList<>();
}
