package com.solveria.core.legal.infrastructure.jpa;

import com.solveria.core.legal.domain.model.vo.AddendumStatus;
import com.solveria.core.shared.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(
    name = "legal_contract_addendum",
    indexes = {
      @Index(name = "idx_legal_addendum_addendum_id", columnList = "addendum_id"),
      @Index(name = "idx_legal_addendum_contract_id", columnList = "contract_id"),
      @Index(name = "idx_legal_addendum_tenant_id", columnList = "tenant_id")
    })
@Getter
@Setter
@ToString(exclude = "contract")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractAddendumJpa extends BaseEntity {

  @Column(name = "addendum_id", nullable = false, unique = true)
  private UUID addendumId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "contract_id", referencedColumnName = "contract_id", nullable = false)
  private ContractJpa contract;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private AddendumStatus status;

  @Column(name = "effective_from", nullable = false)
  private LocalDate effectiveFrom;

  @Column(name = "effective_to", nullable = false)
  private LocalDate effectiveTo;

  @Embedded private SalaryTermsEmbeddable salaryTerms;

  @Embedded private ComplianceSnapshotEmbeddable complianceSnapshot;
}
