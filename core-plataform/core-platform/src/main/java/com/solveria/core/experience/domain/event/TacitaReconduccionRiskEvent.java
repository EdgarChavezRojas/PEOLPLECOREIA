package com.solveria.core.experience.domain.event;

import com.solveria.core.shared.events.DomainEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Evento (Async): Alerta de riesgo de tácita reconducción generada por IA (T-90 días). El modelo
 * predictivo detecta contratos a plazo fijo próximos a vencer sin decisión de renovación o
 * terminación. Trigger: AiPredictionModelAlert.
 */
public record TacitaReconduccionRiskEvent(
    UUID modelId,
    UUID contractId,
    UUID personId,
    LocalDate deadline,
    BigDecimal financialImpact,
    int daysUntilExpiry,
    UUID tenantId,
    Instant occurredAt)
    implements DomainEvent {

  public TacitaReconduccionRiskEvent(
      UUID modelId,
      UUID contractId,
      UUID personId,
      LocalDate deadline,
      BigDecimal financialImpact,
      int daysUntilExpiry,
      UUID tenantId) {
    this(
        modelId,
        contractId,
        personId,
        deadline,
        financialImpact,
        daysUntilExpiry,
        tenantId,
        Instant.now());
  }
}
