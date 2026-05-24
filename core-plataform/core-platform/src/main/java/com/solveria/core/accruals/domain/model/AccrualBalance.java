package com.solveria.core.accruals.domain.model;

import com.solveria.core.accruals.domain.event.AccrualBalanceDeductedEvent;
import com.solveria.core.accruals.domain.event.AccrualBalanceUpdatedEvent;
import com.solveria.core.accruals.domain.event.LeaveRequestManagerApprovedEvent;
import com.solveria.core.accruals.domain.event.LeaveRequestManagerRejectedEvent;
import com.solveria.core.accruals.domain.event.LeaveRequestSubmittedEvent;
import com.solveria.core.accruals.domain.event.SeniorityMilestoneReachedEvent;
import com.solveria.core.accruals.domain.event.VacationBalanceThresholdLowEvent;
import com.solveria.core.accruals.domain.exception.InsufficientAccrualBalanceException;
import com.solveria.core.accruals.domain.exception.InvalidAccrualStateException;
import com.solveria.core.accruals.domain.exception.InvalidLeaveStateException;
import com.solveria.core.accruals.domain.model.vo.AccrualBalanceType;
import com.solveria.core.accruals.domain.model.vo.AccrualUnit;
import com.solveria.core.accruals.domain.model.vo.LeaveStatus;
import com.solveria.core.accruals.domain.model.vo.SeniorityMilestone;
import com.solveria.core.accruals.domain.model.vo.SenioritySpan;
import com.solveria.core.accruals.domain.policy.BolivianSeniorityScale;
import com.solveria.core.accruals.domain.policy.VacationScalePolicy;
import com.solveria.core.shared.outbox.domain.DomainRoot;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AccrualBalance extends DomainRoot {

  private UUID balanceId;
  private UUID relationshipId;
  private AccrualBalanceType balanceType;
  private AccrualUnit unit;
  private BigDecimal currentBalance;
  private BigDecimal initialBalance;
  private BigDecimal daysAccruedYtd;
  private BigDecimal daysTakenYtd;
  private LocalDate lastAccrualDate;
  private UUID tenantId;
  private List<LeaveTransaction> leaveTransactions;
  private List<SeniorityMilestone> seniorityMilestones;

  public AccrualBalance() {}

  public AccrualBalance(
      UUID balanceId,
      UUID relationshipId,
      AccrualBalanceType balanceType,
      AccrualUnit unit,
      BigDecimal currentBalance,
      BigDecimal initialBalance,
      BigDecimal daysAccruedYtd,
      BigDecimal daysTakenYtd,
      LocalDate lastAccrualDate,
      UUID tenantId,
      List<LeaveTransaction> leaveTransactions,
      List<SeniorityMilestone> seniorityMilestones) {
    this.balanceId = balanceId;
    this.relationshipId = relationshipId;
    this.balanceType = balanceType;
    this.unit = unit;
    this.currentBalance = currentBalance;
    this.initialBalance = initialBalance;
    this.daysAccruedYtd = daysAccruedYtd;
    this.daysTakenYtd = daysTakenYtd;
    this.lastAccrualDate = lastAccrualDate;
    this.tenantId = tenantId;
    this.leaveTransactions = leaveTransactions;
    this.seniorityMilestones = seniorityMilestones;
  }

  public static AccrualBalance open(
      UUID relationshipId,
      AccrualBalanceType balanceType,
      AccrualUnit unit,
      BigDecimal currentBalance,
      LocalDate lastAccrualDate,
      UUID tenantId) {
    if (relationshipId == null) {
      throw new IllegalArgumentException("relationshipId is required");
    }
    if (balanceType == null) {
      throw new IllegalArgumentException("balanceType is required");
    }
    if (unit == null) {
      throw new IllegalArgumentException("unit is required");
    }
    if (currentBalance == null) {
      throw new IllegalArgumentException("currentBalance is required");
    }
    if (currentBalance.signum() < 0) {
      throw new IllegalArgumentException("currentBalance must be non-negative");
    }
    if (tenantId == null) {
      throw new IllegalArgumentException("tenantId is required");
    }
    return new AccrualBalance(
        UUID.randomUUID(),
        relationshipId,
        balanceType,
        unit,
        currentBalance,
        BigDecimal.ZERO,
        BigDecimal.ZERO,
        BigDecimal.ZERO,
        lastAccrualDate,
        tenantId,
        new ArrayList<>(),
        new ArrayList<>());
  }

  public LeaveTransaction requestLeave(
      LocalDate startDate, LocalDate endDate, BigDecimal chargeableDays) {
    requireVacationDays();
    if (startDate == null || endDate == null) {
      throw new IllegalArgumentException("startDate and endDate are required");
    }
    if (chargeableDays == null || chargeableDays.signum() <= 0) {
      throw new IllegalArgumentException("chargeableDays must be positive");
    }
    if (currentBalance.compareTo(chargeableDays) < 0) {
      registerEvent(
          VacationBalanceThresholdLowEvent.now(balanceId, chargeableDays, currentBalance));
      throw new InsufficientAccrualBalanceException(balanceId, chargeableDays, currentBalance);
    }
    LeaveTransaction transaction =
        LeaveTransaction.pending(balanceId, startDate, endDate, chargeableDays);
    if (leaveTransactions == null) {
      leaveTransactions = new ArrayList<>();
    }
    leaveTransactions.add(transaction);
    registerEvent(
        LeaveRequestSubmittedEvent.now(
            balanceId, transaction.getTransactionId(), startDate, endDate, chargeableDays));
    return transaction;
  }

  public void approveLeave(UUID transactionId, UUID tenantId) {
    LeaveTransaction transaction = findTransaction(transactionId);
    if (transaction.getStatus() != LeaveStatus.PENDING) {
      throw new InvalidLeaveStateException("leave transaction is not pending");
    }
    transaction.approve();
    deduct(transaction.getDaysRequested());
    registerEvent(
        LeaveRequestManagerApprovedEvent.now(
            balanceId, transaction.getTransactionId(), transaction.getDaysRequested(), tenantId));
    registerEvent(AccrualBalanceDeductedEvent.now(balanceId, transaction.getDaysRequested()));
  }

  public void rejectLeave(UUID transactionId) {
    LeaveTransaction transaction = findTransaction(transactionId);
    if (transaction.getStatus() != LeaveStatus.PENDING) {
      throw new InvalidLeaveStateException("leave transaction is not pending");
    }
    transaction.reject();
    registerEvent(LeaveRequestManagerRejectedEvent.now(balanceId, transaction.getTransactionId()));
  }

  public void accrueVacation(int yearsOfService, LocalDate accrualDate) {
    requireVacationDays();
    int days = VacationScalePolicy.vacationDaysForYears(yearsOfService);
    if (days <= 0) {
      return;
    }
    currentBalance = currentBalance.add(BigDecimal.valueOf(days));
    lastAccrualDate = accrualDate != null ? accrualDate : LocalDate.now();
    registerEvent(
        AccrualBalanceUpdatedEvent.now(relationshipId, balanceType.name(), currentBalance));
  }

  public void addSeniorityMilestone(SeniorityMilestone milestone) {
    if (milestone == null) {
      throw new IllegalArgumentException("milestone is required");
    }
    if (seniorityMilestones == null) {
      seniorityMilestones = new ArrayList<>();
    }
    seniorityMilestones.add(milestone);
    int years = milestone.monthsCompleted() / 12;
    int smMultiplier = BolivianSeniorityScale.smMultiplierFor(years);
    if (smMultiplier > 0) {
      registerEvent(SeniorityMilestoneReachedEvent.now(relationshipId, years, smMultiplier));
    }
  }

  public SenioritySpan computeSenioritySpan(LocalDate hireDate) {
    return SenioritySpan.between(hireDate, LocalDate.now());
  }

  private void requireVacationDays() {
    if (balanceType != AccrualBalanceType.VACATION || unit != AccrualUnit.DAYS) {
      throw new InvalidAccrualStateException("balance type must be VACATION and unit DAYS");
    }
    if (currentBalance == null) {
      currentBalance = BigDecimal.ZERO;
    }
    if (currentBalance.signum() < 0) {
      throw new InvalidAccrualStateException("currentBalance must be non-negative");
    }
  }

  private void deduct(BigDecimal days) {
    if (days == null || days.signum() <= 0) {
      throw new InvalidAccrualStateException("days must be positive");
    }
    BigDecimal newBalance = currentBalance.subtract(days);
    if (newBalance.signum() < 0) {
      throw new InvalidAccrualStateException("currentBalance cannot be negative");
    }
    currentBalance = newBalance;
  }

  private LeaveTransaction findTransaction(UUID transactionId) {
    if (transactionId == null) {
      throw new IllegalArgumentException("transactionId is required");
    }
    if (leaveTransactions == null || leaveTransactions.isEmpty()) {
      throw new InvalidLeaveStateException("leave transaction not found");
    }
    return leaveTransactions.stream()
        .filter(tx -> transactionId.equals(tx.getTransactionId()))
        .findFirst()
        .orElseThrow(() -> new InvalidLeaveStateException("leave transaction not found"));
  }

  // Getters
  public UUID getBalanceId() {
    return balanceId;
  }

  public UUID getRelationshipId() {
    return relationshipId;
  }

  public AccrualBalanceType getBalanceType() {
    return balanceType;
  }

  public AccrualUnit getUnit() {
    return unit;
  }

  public BigDecimal getCurrentBalance() {
    return currentBalance;
  }

  public BigDecimal getInitialBalance() {
    return initialBalance;
  }

  public BigDecimal getDaysAccruedYtd() {
    return daysAccruedYtd;
  }

  public BigDecimal getDaysTakenYtd() {
    return daysTakenYtd;
  }

  public LocalDate getLastAccrualDate() {
    return lastAccrualDate;
  }

  public UUID getTenantId() {
    return tenantId;
  }

  public List<LeaveTransaction> getLeaveTransactions() {
    return leaveTransactions;
  }

  public List<SeniorityMilestone> getSeniorityMilestones() {
    return seniorityMilestones;
  }
}
