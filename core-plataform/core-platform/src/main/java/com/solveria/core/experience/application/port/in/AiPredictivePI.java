package com.solveria.core.experience.application.port.in;

import com.solveria.core.experience.application.command.RegisterPredictionModelCommand;
import com.solveria.core.experience.domain.model.vo.RiskAlert;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Primary Port (Inbound): AI Predictive Analytics. Expone operaciones del motor predictivo de IA.
 * Genera alertas de riesgo para pasivos laborales y compliance.
 */
public interface AiPredictivePI {

  /** Registrar un nuevo modelo predictivo IA. */
  UUID registerPredictionModel(RegisterPredictionModelCommand cmd);

  /** Generar alerta de tácita reconducción (T-90 días). */
  RiskAlert generateTacitaReconduccionAlert(
      UUID modelId,
      UUID contractId,
      UUID personId,
      BigDecimal financialImpact,
      int daysUntilExpiry);

  /** Generar alerta de umbral disciplinario alcanzado. */
  RiskAlert generateDisciplinaryAlert(
      UUID modelId, UUID personId, int memorandumCount, int periodMonths);

  /** Generar alerta de quinquenio inminente. */
  RiskAlert generateQuinquenioLiabilityAlert(
      UUID modelId, int quinquenioCount, BigDecimal totalImpact);

  /** Descartar una alerta. */
  void dismissAlert(UUID modelId, UUID alertId);

  /** Consultar alertas activas de un modelo. */
  List<RiskAlert> getActiveAlerts(UUID modelId);
}
