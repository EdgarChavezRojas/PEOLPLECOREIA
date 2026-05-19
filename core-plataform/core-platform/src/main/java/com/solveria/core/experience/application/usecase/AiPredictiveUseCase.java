package com.solveria.core.experience.application.usecase;

import com.solveria.core.experience.application.command.RegisterPredictionModelCommand;
import com.solveria.core.experience.application.port.in.AiPredictivePI;
import com.solveria.core.experience.application.port.out.PredictionModelPO;
import com.solveria.core.experience.domain.model.PredictionModel;
import com.solveria.core.experience.domain.model.vo.RiskAlert;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.solveria.core.security.context.SecurityTenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case: AI Predictive Analytics. Genera alertas de riesgo basadas en análisis predictivo.
 * Invariante AI_Neutrality: no sugiere acciones que violen umbrales legales. SMN vigente 2026: Bs
 * 3,300.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiPredictiveUseCase implements AiPredictivePI {

  private static final BigDecimal SMN = new BigDecimal("3300.00");
  private final PredictionModelPO predictionModelPO;

  @Override
  @Transactional
  public UUID registerPredictionModel(RegisterPredictionModelCommand cmd) {
    log.info(
        "event=PREDICTION_MODEL_REGISTER modelType={} version={} ",
        cmd.modelType(),
        cmd.version());
    UUID tenantId = UUID.fromString(SecurityTenantContext.getCurrentTenantId());
    PredictionModel model = PredictionModel.create(cmd.modelType(), cmd.version(),tenantId );
    predictionModelPO.save(model);

    log.info(
        "event=PREDICTION_MODEL_REGISTERED modelId={} modelType={}",
        model.getModelId(),
        model.getModelType());
    return model.getModelId();
  }

  @Override
  @Transactional
  public RiskAlert generateTacitaReconduccionAlert(
      UUID modelId,
      UUID contractId,
      UUID personId,
      BigDecimal financialImpact,
      int daysUntilExpiry) {
    log.info(
        "event=TACITA_RECONDUCCION_RISK modelId={} contractId={} days={}",
        modelId,
        contractId,
        daysUntilExpiry);

    PredictionModel model = findModel(modelId);
    RiskAlert alert =
        model.generateTacitaReconduccionAlert(
            contractId, personId, financialImpact, daysUntilExpiry, SMN);
    predictionModelPO.save(model);

    log.info(
        "event=TACITA_RECONDUCCION_ALERT_GENERATED alertId={} severity={}",
        alert.alertId(),
        alert.severity());
    return alert;
  }

  @Override
  @Transactional
  public RiskAlert generateDisciplinaryAlert(
      UUID modelId, UUID personId, int memorandumCount, int periodMonths) {
    log.info(
        "event=DISCIPLINARY_THRESHOLD modelId={} personId={} count={}",
        modelId,
        personId,
        memorandumCount);

    PredictionModel model = findModel(modelId);
    RiskAlert alert =
        model.generateDisciplinaryThresholdAlert(personId, memorandumCount, periodMonths);
    predictionModelPO.save(model);

    log.info("event=DISCIPLINARY_ALERT_GENERATED alertId={}", alert.alertId());
    return alert;
  }

  @Override
  @Transactional
  public RiskAlert generateQuinquenioLiabilityAlert(
      UUID modelId, int quinquenioCount, BigDecimal totalImpact) {
    log.info(
        "event=QUINQUENIO_LIABILITY modelId={} count={} impact={}",
        modelId,
        quinquenioCount,
        totalImpact);

    PredictionModel model = findModel(modelId);
    RiskAlert alert = model.generateQuinquenioLiabilityAlert(quinquenioCount, totalImpact, SMN);
    predictionModelPO.save(model);

    log.info("event=QUINQUENIO_LIABILITY_ALERT_GENERATED alertId={}", alert.alertId());
    return alert;
  }

  @Override
  @Transactional
  public void dismissAlert(UUID modelId, UUID alertId) {
    log.info("event=ALERT_DISMISSED modelId={} alertId={}", modelId, alertId);
    PredictionModel model = findModel(modelId);
    model.dismissAlert(alertId);
    predictionModelPO.save(model);
  }

  @Override
  @Transactional(readOnly = true)
  public List<RiskAlert> getActiveAlerts(UUID modelId) {
    PredictionModel model = findModel(modelId);
    return model.getAlerts().stream().filter(a -> !a.isDismissed()).toList();
  }

  private PredictionModel findModel(UUID modelId) {
    return predictionModelPO
        .findById(modelId)
        .orElseThrow(
            () -> new IllegalArgumentException("PredictionModel no encontrado: " + modelId));
  }
}
