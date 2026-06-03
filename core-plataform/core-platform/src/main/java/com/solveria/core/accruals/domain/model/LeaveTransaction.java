package com.solveria.core.accruals.domain.model;

import com.solveria.core.accruals.domain.model.vo.LeaveStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;

@Getter
public class LeaveTransaction {

  private UUID transactionId;
  private UUID balanceId;
  private UUID tenantId;
  private LocalDate startDate;
  private LocalDate endDate;
  private BigDecimal daysRequested;
  private LeaveStatus status;

  public LeaveTransaction() {}

  // Este constructor estaba mal porque declaraste tenantId localmente y no lo usas

  // Este constructor es el correcto ahora
  public LeaveTransaction(
      UUID transactionId,
      UUID balanceId,
      UUID tenantId,
      LocalDate start,
      LocalDate end,
      BigDecimal days,
      LeaveStatus status) {
    this.transactionId = transactionId;
    this.balanceId = balanceId;
    this.tenantId = tenantId;
    this.startDate = start;
    this.endDate = end;
    this.daysRequested = days;
    this.status = status;
  }

  public static LeaveTransaction pending(
      UUID balanceId, UUID tenantId, LocalDate start, LocalDate end, BigDecimal days) {
    return new LeaveTransaction(
        UUID.randomUUID(), balanceId, tenantId, start, end, days, LeaveStatus.PENDING);
  }

  public void approve() {
    status = LeaveStatus.APPROVED;
  }

  public void reject() {
    status = LeaveStatus.REJECTED;
  }
}
