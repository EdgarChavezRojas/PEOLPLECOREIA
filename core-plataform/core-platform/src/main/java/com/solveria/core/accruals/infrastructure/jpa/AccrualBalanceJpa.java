package com.solveria.core.accruals.infrastructure.jpa;

import com.solveria.core.accruals.domain.model.vo.AccrualBalanceType;
import com.solveria.core.accruals.domain.model.vo.AccrualUnit;
import com.solveria.core.shared.base.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "accrual_balance",
    indexes = {
      @Index(name = "idx_accrual_balance_relationship", columnList = "relationship_id"),
      @Index(name = "idx_accrual_balance_tenant", columnList = "tenant_id"),
      @Index(name = "idx_accrual_balance_type", columnList = "balance_type")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccrualBalanceJpa extends BaseEntity {

  @Id
  @Column(name = "balance_id")
  private UUID balanceId;

  @Column(name = "relationship_id", nullable = false)
  private UUID relationshipId;

  @Enumerated(EnumType.STRING)
  @Column(name = "balance_type", nullable = false)
  private AccrualBalanceType balanceType;

  @Enumerated(EnumType.STRING)
  @Column(name = "unit", nullable = false)
  private AccrualUnit unit;

  @Column(name = "current_balance", nullable = false)
  private BigDecimal currentBalance;

  @Column(name = "initial_balance")
  private BigDecimal initialBalance;

  @Column(name = "days_accrued_ytd")
  private BigDecimal daysAccruedYtd;

  @Column(name = "days_taken_ytd")
  private BigDecimal daysTakenYtd;

  @Column(name = "last_accrual_date")
  private LocalDate lastAccrualDate;


  @OneToMany(mappedBy = "balance", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<LeaveTransactionJpa> leaveTransactions = new ArrayList<>();

  @OneToMany(mappedBy = "balance", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<SeniorityMilestoneJpa> seniorityMilestones = new ArrayList<>();
}
