package com.solveria.core.financial.domain.event;

import com.solveria.core.financial.domain.model.vo.IndemnizableTrimSnapshot;
import com.solveria.core.shared.events.DomainEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Evento (Sync): Genera borrador del Finiquito con promedio últimos 90 días. Trigger:
 * LiquidationProcessCompleted.
 */
public record LiquidationCalculatedEvent(
    UUID relationshipId,
    UUID personId,
    UUID liquidationId,
    BigDecimal averageSalary,
    BigDecimal totalLiquidation,
    BigDecimal netAmount,
    boolean includesDesahucio,
    IndemnizableTrimSnapshot trimSnapshot,
    Instant occurredAt)
    implements DomainEvent {

  public LiquidationCalculatedEvent(
      UUID relationshipId,
      UUID personId,
      UUID liquidationId,
      BigDecimal averageSalary,
      BigDecimal totalLiquidation,
      BigDecimal netAmount,
      boolean includesDesahucio,
      IndemnizableTrimSnapshot trimSnapshot) {
    this(
        relationshipId,
        personId,
        liquidationId,
        averageSalary,
        totalLiquidation,
        netAmount,
        includesDesahucio,
        trimSnapshot,
        Instant.now());
  }
}
