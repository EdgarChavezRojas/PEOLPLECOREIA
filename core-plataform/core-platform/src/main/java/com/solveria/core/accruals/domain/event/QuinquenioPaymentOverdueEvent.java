package com.solveria.core.accruals.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Emitido al cumplir 31 días desde la solicitud sin registro de pago. Trigger: Day31WithoutPayment.
 * Aplica automáticamente la Multa del 30% sobre el monto total de la indemnización, según normativa
 * boliviana. El sistema no permite ignorar la multa una vez disparada.
 */
public record QuinquenioPaymentOverdueEvent(
    UUID personId, UUID provisionId, BigDecimal penaltyAmount, Instant occurredAt)
    implements DomainEvent {

  public QuinquenioPaymentOverdueEvent {
    if (personId == null) {
      throw new IllegalArgumentException("personId es requerido");
    }
    if (provisionId == null) {
      throw new IllegalArgumentException("provisionId es requerido");
    }
    if (penaltyAmount == null || penaltyAmount.signum() <= 0) {
      throw new IllegalArgumentException("penaltyAmount debe ser positivo");
    }
    if (occurredAt == null) {
      throw new IllegalArgumentException("occurredAt es requerido");
    }
  }

  public static QuinquenioPaymentOverdueEvent now(
      UUID personId, UUID provisionId, BigDecimal penaltyAmount) {
    return new QuinquenioPaymentOverdueEvent(personId, provisionId, penaltyAmount, Instant.now());
  }
}
