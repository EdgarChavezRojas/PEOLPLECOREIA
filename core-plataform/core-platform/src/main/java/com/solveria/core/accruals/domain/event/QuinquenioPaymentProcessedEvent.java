package com.solveria.core.accruals.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record QuinquenioPaymentProcessedEvent(
    UUID provisionId, UUID relationshipId, LocalDate paymentDate, Instant occurredAt)
    implements DomainEvent {

  public QuinquenioPaymentProcessedEvent {
    if (provisionId == null) {
      throw new IllegalArgumentException("provisionId es requerido");
    }
    if (relationshipId == null) {
      throw new IllegalArgumentException("relationshipId es requerido");
    }
    if (paymentDate == null) {
      throw new IllegalArgumentException("paymentDate es requerido");
    }
    if (occurredAt == null) {
      throw new IllegalArgumentException("occurredAt es requerido");
    }
  }

  public static QuinquenioPaymentProcessedEvent now(
      UUID provisionId, UUID relationshipId, LocalDate paymentDate) {
    return new QuinquenioPaymentProcessedEvent(
        provisionId, relationshipId, paymentDate, Instant.now());
  }
}
