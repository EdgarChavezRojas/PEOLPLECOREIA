package com.solveria.core.financial.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Evento (Async): Quinquenio solicitado — inicia cronómetro de 30 días para pago.
 * Trigger: Empleado cumple 60 meses continuos de antigüedad.
 *
 * <p>El quinquenio es exento de RC-IVA y deducciones de Gestora.
 * El paymentDeadline marca la fecha límite legal para efectivizar el pago.
 */
public record QuinquenioRequestedEvent(
    UUID personId,
    BigDecimal quinquenioAmount,
    BigDecimal averageSalary,
    LocalDate paymentDeadline,
    Instant occurredAt)
    implements DomainEvent {

  public QuinquenioRequestedEvent(
      UUID personId,
      BigDecimal quinquenioAmount,
      BigDecimal averageSalary,
      LocalDate paymentDeadline) {
    this(personId, quinquenioAmount, averageSalary, paymentDeadline, Instant.now());
  }
}
