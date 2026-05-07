package com.solveria.core.accruals.domain.model;

import com.solveria.core.accruals.domain.model.vo.LeaveStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveTransaction {

  private UUID transactionId;
  private UUID balanceId;
  private LocalDate startDate;
  private LocalDate endDate;
  private BigDecimal daysRequested;
  private LeaveStatus status;

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
    return LeaveTransaction.builder()
        .transactionId(UUID.randomUUID())
        .balanceId(balanceId)
        .startDate(startDate)
        .endDate(endDate)
        .daysRequested(daysRequested)
        .status(LeaveStatus.PENDING)
        .build();
  }

  public void approve() {
    status = LeaveStatus.APPROVED;
  }

  public void reject() {
    status = LeaveStatus.REJECTED;
  }
}
