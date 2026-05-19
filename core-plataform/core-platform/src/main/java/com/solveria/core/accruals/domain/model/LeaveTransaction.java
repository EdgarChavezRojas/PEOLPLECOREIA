package com.solveria.core.accruals.domain.model;

import com.solveria.core.accruals.domain.model.vo.LeaveStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class LeaveTransaction {

  private UUID transactionId;
  private UUID balanceId;
  private LocalDate startDate;
  private LocalDate endDate;
  private BigDecimal daysRequested;
  private LeaveStatus status;

  public LeaveTransaction() {
  }

  public LeaveTransaction(UUID transactionId, UUID balanceId, LocalDate startDate, LocalDate endDate, BigDecimal daysRequested, LeaveStatus status) {
    this.transactionId = transactionId;
    this.balanceId = balanceId;
    this.startDate = startDate;
    this.endDate = endDate;
    this.daysRequested = daysRequested;
    this.status = status;
  }

  public static LeaveTransaction pending(
          UUID balanceId, LocalDate startDate, LocalDate endDate, BigDecimal daysRequested) {
    if (balanceId == null) {
      throw new IllegalArgumentException("balanceId is required");
    }
    if (startDate == null || endDate == null) {
      throw new IllegalArgumentException("startDate and endDate are required");
    }
    if (daysRequested == null || daysRequested.signum() <= 0) {
      throw new IllegalArgumentException("daysRequested must be positive");
    }
    return new LeaveTransaction(
            UUID.randomUUID(),
            balanceId,
            startDate,
            endDate,
            daysRequested,
            LeaveStatus.PENDING
    );
  }

  public void approve() {
    status = LeaveStatus.APPROVED;
  }

  public void reject() {
    status = LeaveStatus.REJECTED;
  }

  // Getters
  public UUID getTransactionId() { return transactionId; }
  public UUID getBalanceId() { return balanceId; }
  public LocalDate getStartDate() { return startDate; }
  public LocalDate getEndDate() { return endDate; }
  public BigDecimal getDaysRequested() { return daysRequested; }
  public LeaveStatus getStatus() { return status; }
}