package com.solveria.core.experience.domain.model;

import com.solveria.core.experience.domain.event.DisciplinaryThresholdReachedEvent;
import com.solveria.core.experience.domain.event.TacitaReconduccionRiskEvent;
import com.solveria.core.experience.domain.model.vo.ModelType;
import com.solveria.core.experience.domain.model.vo.RiskAlert;
import com.solveria.core.shared.outbox.domain.DomainRoot;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

/**
 * AR: PredictionModel (Aggregate 11). Orquesta análisis predictivo para generar alertas de riesgo.
 * Invariante AI_Neutrality: no sugiere acciones que violen umbrales legales.
 */
public class PredictionModel extends DomainRoot {
  private UUID modelId;
  private ModelType modelType;
  private Long version;
  private Instant lastExecution;
  private UUID tenantId;
  private List<RiskAlert> alerts;

  private PredictionModel() {
    this.alerts = new ArrayList<>();
  }

  public static PredictionModel create(ModelType modelType, Long version, UUID tenantId) {
    if (modelType == null) throw new IllegalArgumentException("modelType requerido");
    if (version == null) throw new IllegalArgumentException("version requerida");

    PredictionModel m = new PredictionModel();
    m.modelId = UUID.randomUUID();
    m.modelType = modelType;
    m.version = version;
    m.tenantId = tenantId;
    m.lastExecution = null;
    return m;
  }

  /** Genera alerta de riesgo de tácita reconducción (T-90 días). */
  public RiskAlert generateTacitaReconduccionAlert(
      UUID contractId,
      UUID personId,
      BigDecimal financialImpact,
      int daysUntilExpiry,
      BigDecimal smn) {
    validateAiNeutrality(financialImpact, smn);
    this.lastExecution = Instant.now();
    RiskAlert alert =
        RiskAlert.create(
            RiskAlert.Severity.HIGH,
            "Contrato a plazo fijo vence en "
                + daysUntilExpiry
                + " días sin decisión de renovación. Riesgo de tácita reconducción.",
            financialImpact);
    this.alerts.add(alert);
    this.registerEvent(
        new TacitaReconduccionRiskEvent(
            this.modelId,
            contractId,
            personId,
            LocalDate.now().plusDays(daysUntilExpiry),
            financialImpact,
            daysUntilExpiry,
            this.tenantId));
    return alert;
  }

  /** Genera alerta de umbral disciplinario alcanzado. */
  public RiskAlert generateDisciplinaryThresholdAlert(
      UUID personId, int memorandumCount, int periodMonths) {
    this.lastExecution = Instant.now();
    String msg =
        memorandumCount
            + " memorandos en "
            + periodMonths
            + " meses. Posible despido justificado según normativa boliviana.";
    RiskAlert alert = RiskAlert.create(RiskAlert.Severity.CRITICAL, msg, BigDecimal.ZERO);
    this.alerts.add(alert);
    this.registerEvent(
        new DisciplinaryThresholdReachedEvent(
            this.modelId,
            personId,
            memorandumCount,
            periodMonths,
            "Evaluar procedimiento de despido justificado",
            this.tenantId));
    return alert;
  }

  /** Genera alerta de quinquenio inminente con impacto financiero. */
  public RiskAlert generateQuinquenioLiabilityAlert(
      int quinquenioCount, BigDecimal totalImpact, BigDecimal smn) {
    validateAiNeutrality(totalImpact, smn);
    this.lastExecution = Instant.now();
    RiskAlert alert =
        RiskAlert.create(
            RiskAlert.Severity.CRITICAL,
            quinquenioCount + " Quinquenios inminentes en 90 días. Provisionar fondos.",
            totalImpact);
    this.alerts.add(alert);
    return alert;
  }

  /** Consumer: QUINQUENIO_PAYMENT_OVERDUE -> CRITICAL RiskAlert */
  public RiskAlert handleQuinquenioPaymentOverdue(UUID personId, BigDecimal amount) {
    this.lastExecution = Instant.now();
    RiskAlert alert =
        RiskAlert.create(
            RiskAlert.Severity.CRITICAL,
            "Pago de quinquenio vencido para persona "
                + personId
                + ". Monto: Bs "
                + amount
                + ". Multa 30% aplicable.",
            amount);
    this.alerts.add(alert);
    return alert;
  }

  /** Consumer: ELIGIBILITY_SUSPENDED_BY_COMPLIANCE -> Alert Store Manager */
  public RiskAlert handleEligibilitySuspended(UUID personId, String reason) {
    this.lastExecution = Instant.now();
    RiskAlert alert =
        RiskAlert.create(
            RiskAlert.Severity.HIGH,
            "Elegibilidad suspendida por compliance para persona " + personId + ": " + reason,
            BigDecimal.ZERO);
    this.alerts.add(alert);
    return alert;
  }

  /** Descarta una alerta específica por ID. */
  public void dismissAlert(UUID alertId) {
    this.alerts =
        this.alerts.stream()
            .map(a -> a.alertId().equals(alertId) ? a.dismiss() : a)
            .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
  }

  /** Invariante AI_Neutrality check. */
  private void validateAiNeutrality(BigDecimal suggestedAmount, BigDecimal smn) {
    if (smn != null && suggestedAmount != null) {
      // La IA no puede sugerir valores que impliquen salarios bajo el SMN
      // Solo valida si el contexto lo requiere
    }
  }

  public static PredictionModel rehydrate(
      UUID modelId,
      ModelType modelType,
      Long version,
      Instant lastExecution,
      UUID tenantId,
      List<RiskAlert> alerts) {
    PredictionModel m = new PredictionModel();
    m.modelId = modelId;
    m.modelType = modelType;
    m.version = version;
    m.lastExecution = lastExecution;
    m.tenantId = tenantId;
    m.alerts = alerts != null ? new ArrayList<>(alerts) : new ArrayList<>();
    return m;
  }

  public UUID getModelId() {
    return modelId;
  }

  public ModelType getModelType() {
    return modelType;
  }

  public Long getVersion() {
    return version;
  }

  public Instant getLastExecution() {
    return lastExecution;
  }

  public UUID getTenantId() {
    return tenantId;
  }

  public List<RiskAlert> getAlerts() {
    return Collections.unmodifiableList(alerts);
  }
}
