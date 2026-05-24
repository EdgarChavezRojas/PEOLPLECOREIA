package com.solveria.core.financial.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Evento (Async): Elegibilidad para Segundo Aguinaldo (Esfuerzo por Bolivia) confirmada. Trigger:
 * GdpGrowthExceedsThreshold.
 *
 * <p>Se emite cuando el crecimiento del PIB supera el umbral legal definido por el Decreto Supremo
 * vigente, activando la obligación de pago del Segundo Aguinaldo para el empleado identificado.
 *
 * <p>El provisionalAmount representa el monto provisorio calculado sobre el promedio indemnizable
 * (P15) sujeto a confirmación en el cierre de nómina.
 */
public record SegundoAguinaldoEligibilityMetEvent(
    UUID personId, BigDecimal provisionalAmount, Instant occurredAt) implements DomainEvent {

  public SegundoAguinaldoEligibilityMetEvent(UUID personId, BigDecimal provisionalAmount) {
    this(personId, provisionalAmount, Instant.now());
  }
}
