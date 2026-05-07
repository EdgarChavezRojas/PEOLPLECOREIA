package com.solveria.core.accruals.domain.model;

import com.solveria.core.accruals.domain.event.AccrualEvent;
import com.solveria.core.accruals.domain.event.AccrualEventType;
import com.solveria.core.accruals.domain.event.QuinquenioEligibilityReachedEvent;
import com.solveria.core.accruals.domain.event.QuinquenioPaymentOverdueEvent;
import com.solveria.core.accruals.domain.policy.QuinquenioPolicy;
import com.solveria.core.shared.events.DomainEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuinquenioProvision {

  private UUID provisionId;
  private UUID relationshipId;
  private BigDecimal totalAccumulated;
  private boolean penaltyActive;
  private UUID tenantId;

  @Builder.Default private transient List<DomainEvent> domainEvents = new ArrayList<>();

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
    return QuinquenioProvision.builder()
        .provisionId(UUID.randomUUID())
        .relationshipId(relationshipId)
        .totalAccumulated(totalAccumulated)
        .penaltyActive(false)
        .tenantId(tenantId)
        .build();
  }

  public void addMonthlyProvision(BigDecimal amount) {
    if (amount == null || amount.signum() <= 0) {
      throw new IllegalArgumentException("amount must be positive");
    }
    totalAccumulated = totalAccumulated.add(amount);
  }

  public void markEligible() {
    addDomainEvent(AccrualEvent.now(AccrualEventType.QUINQUENIO_ELIGIBILITY_REACHED));
    addDomainEvent(QuinquenioEligibilityReachedEvent.now(
        relationshipId, LocalDate.now()));
  }

  public void requestPayment(LocalDate requestDate) {
    if (requestDate == null) {
      throw new IllegalArgumentException("requestDate is required");
    }
    addDomainEvent(AccrualEvent.now(AccrualEventType.QUINQUENIO_REQUESTED));
  }

  public void finalizeCalculation(BigDecimal averageLast90Days) {
    if (averageLast90Days == null || averageLast90Days.signum() <= 0) {
      throw new IllegalArgumentException("averageLast90Days must be positive");
    }
    totalAccumulated = averageLast90Days;
    addDomainEvent(AccrualEvent.now(AccrualEventType.QUINQUENIO_CALCULATION_FINALIZED));
  }

  public void evaluatePenalty(LocalDate requestDate, LocalDate today, LocalDate paymentDate) {
    if (QuinquenioPolicy.isPaymentOverdue(requestDate, today, paymentDate)) {
      penaltyActive = true;
      addDomainEvent(AccrualEvent.now(AccrualEventType.QUINQUENIO_PAYMENT_OVERDUE));
      BigDecimal penalty = totalAccumulated.multiply(QuinquenioPolicy.PENALTY_RATE);
      addDomainEvent(QuinquenioPaymentOverdueEvent.now(
          relationshipId, provisionId, penalty));
    }
  }

  public void markPaid(LocalDate paymentDate) {
    if (paymentDate == null) {
      throw new IllegalArgumentException("paymentDate is required");
    }
    penaltyActive = false;
    addDomainEvent(AccrualEvent.now(AccrualEventType.QUINQUENIO_PAYMENT_PROCESSED));
  }

  public void addDomainEvent(DomainEvent event) {
    if (event == null) {
      return;
    }
    if (domainEvents == null) {
      domainEvents = new ArrayList<>();
    }
    domainEvents.add(event);
  }

  public List<DomainEvent> pullDomainEvents() {
    if (domainEvents == null || domainEvents.isEmpty()) {
      return List.of();
    }
    List<DomainEvent> events = List.copyOf(domainEvents);
    domainEvents.clear();
    return events;
  }
}
