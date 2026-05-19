package com.solveria.core.accruals.domain.model;

import com.solveria.core.accruals.domain.event.QuinquenioCalculationFinalizedEvent;
import com.solveria.core.accruals.domain.event.QuinquenioEligibilityReachedEvent;
import com.solveria.core.accruals.domain.event.QuinquenioPaymentOverdueEvent;
import com.solveria.core.accruals.domain.event.QuinquenioPaymentProcessedEvent;
import com.solveria.core.accruals.domain.event.QuinquenioRequestedEvent;
import com.solveria.core.accruals.domain.policy.QuinquenioPolicy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.solveria.core.shared.outbox.domain.DomainRoot;

public class QuinquenioProvision extends DomainRoot {

  private UUID provisionId;
  private UUID relationshipId;
  private BigDecimal totalAccumulated;
  private boolean penaltyActive;
  private UUID tenantId;

  public QuinquenioProvision() {
  }

  public QuinquenioProvision(UUID provisionId, UUID relationshipId, BigDecimal totalAccumulated, boolean penaltyActive, UUID tenantId) {
    this.provisionId = provisionId;
    this.relationshipId = relationshipId;
    this.totalAccumulated = totalAccumulated;
    this.penaltyActive = penaltyActive;
    this.tenantId = tenantId;
  }

  public static QuinquenioProvision open(
          UUID relationshipId, BigDecimal totalAccumulated, UUID tenantId) {
    if (relationshipId == null) {
      throw new IllegalArgumentException("relationshipId is required");
    }
    if (totalAccumulated == null) {
      throw new IllegalArgumentException("totalAccumulated is required");
    }
    if (tenantId == null) {
      throw new IllegalArgumentException("tenantId is required");
    }
    return new QuinquenioProvision(
            UUID.randomUUID(),
            relationshipId,
            totalAccumulated,
            false,
            tenantId
    );
  }

  public void addMonthlyProvision(BigDecimal amount) {
    if (amount == null || amount.signum() <= 0) {
      throw new IllegalArgumentException("amount must be positive");
    }
    totalAccumulated = totalAccumulated.add(amount);
  }

  public void markEligible() {
    registerEvent(QuinquenioEligibilityReachedEvent.now(
            relationshipId, LocalDate.now()));
  }

  public void requestPayment(LocalDate requestDate) {
    if (requestDate == null) {
      throw new IllegalArgumentException("requestDate is required");
    }
    registerEvent(QuinquenioRequestedEvent.now(provisionId, relationshipId, requestDate));
  }

  public void finalizeCalculation(BigDecimal averageLast90Days) {
    if (averageLast90Days == null || averageLast90Days.signum() <= 0) {
      throw new IllegalArgumentException("averageLast90Days must be positive");
    }
    totalAccumulated = averageLast90Days;
    registerEvent(
            QuinquenioCalculationFinalizedEvent.now(provisionId, relationshipId, averageLast90Days));
  }

  public void evaluatePenalty(LocalDate requestDate, LocalDate today, LocalDate paymentDate) {
    if (QuinquenioPolicy.isPaymentOverdue(requestDate, today, paymentDate)) {
      penaltyActive = true;
      BigDecimal penalty = totalAccumulated.multiply(QuinquenioPolicy.PENALTY_RATE);
      registerEvent(QuinquenioPaymentOverdueEvent.now(
              relationshipId, provisionId, penalty));
    }
  }

  public void markPaid(LocalDate paymentDate) {
    if (paymentDate == null) {
      throw new IllegalArgumentException("paymentDate is required");
    }
    penaltyActive = false;
    registerEvent(QuinquenioPaymentProcessedEvent.now(provisionId, relationshipId, paymentDate));
  }

  // Getters
  public UUID getProvisionId() { return provisionId; }
  public UUID getRelationshipId() { return relationshipId; }
  public BigDecimal getTotalAccumulated() { return totalAccumulated; }
  public boolean isPenaltyActive() { return penaltyActive; }
  public UUID getTenantId() { return tenantId; }
}