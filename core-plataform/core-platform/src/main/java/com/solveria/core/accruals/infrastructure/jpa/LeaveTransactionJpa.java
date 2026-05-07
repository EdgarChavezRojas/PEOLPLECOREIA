package com.solveria.core.accruals.infrastructure.jpa;

import com.solveria.core.accruals.domain.model.vo.LeaveStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "leave_transaction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveTransactionJpa {

  @Id
  @Column(name = "transaction_id")
  private UUID transactionId;

  @Column(name = "balance_id", nullable = false)
  private UUID balanceId;

  @Column(name = "start_date", nullable = false)
  private LocalDate startDate;

  @Column(name = "end_date", nullable = false)
  private LocalDate endDate;

  @Column(name = "days_requested", nullable = false)
  private BigDecimal daysRequested;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private LeaveStatus status;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "balance_id", insertable = false, updatable = false)
  private AccrualBalanceJpa balance;
}
