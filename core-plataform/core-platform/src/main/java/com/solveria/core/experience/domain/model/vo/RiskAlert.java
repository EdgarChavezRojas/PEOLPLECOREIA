package com.solveria.core.experience.domain.model.vo;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Value Object: Alerta de riesgo generada por el motor predictivo. Inmutable. Contiene severidad,
 * mensaje descriptivo e impacto financiero estimado.
 *
 * <p>Ejemplo: "10 Quinquenios en 90 días" con impacto financiero Bs 150,000.
 */
public record RiskAlert(
    UUID alertId,
    Severity severity,
    String message,
    BigDecimal financialImpact,
    boolean isDismissed) {

  /** Severidades de alerta alineadas con el spec BC 6. */
  public enum Severity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
  }

  /** Factory: crea una nueva alerta activa (no descartada). */
  public static RiskAlert create(Severity severity, String message, BigDecimal financialImpact) {
    if (severity == null) {
      throw new IllegalArgumentException("La severidad de la alerta no puede ser nula");
    }
    if (message == null || message.isBlank()) {
      throw new IllegalArgumentException("El mensaje de la alerta no puede estar vacío");
    }
    if (financialImpact == null || financialImpact.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("El impacto financiero no puede ser negativo");
    }
    return new RiskAlert(UUID.randomUUID(), severity, message, financialImpact, false);
  }

  /** Marca la alerta como descartada/atendida. Retorna nueva instancia (inmutable). */
  public RiskAlert dismiss() {
    return new RiskAlert(this.alertId, this.severity, this.message, this.financialImpact, true);
  }

  /**
   * Invariante AI_Neutrality: valida que el impacto financiero no sugiera valores que violen
   * umbrales legales (ej. sueldos bajo SMN).
   */
  public boolean violatesLegalThreshold(BigDecimal smn) {
    // La alerta no puede sugerir un salario por debajo del SMN vigente
    return financialImpact.compareTo(smn) < 0 && severity == Severity.CRITICAL;
  }
}
