package com.solveria.core.accruals.infrastructure.jpa;

import com.solveria.core.accruals.domain.model.vo.SeniorityBaseType;
import com.solveria.core.shared.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "seniority_milestone")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeniorityMilestoneJpa extends BaseEntity {

  @Id
  @Column(name = "milestone_id")
  private UUID milestoneId;

  @Column(name = "balance_id", nullable = false)
  private UUID balanceId;

  @Column(name = "months_completed", nullable = false)
  private int monthsCompleted;

  @Enumerated(EnumType.STRING)
  @Column(name = "base_smn_type", nullable = false)
  private SeniorityBaseType baseSmnType;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "balance_id", insertable = false, updatable = false)
  private AccrualBalanceJpa balance;
}
