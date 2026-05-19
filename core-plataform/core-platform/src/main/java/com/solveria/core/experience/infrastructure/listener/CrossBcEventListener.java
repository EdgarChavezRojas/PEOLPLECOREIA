package com.solveria.core.experience.infrastructure.listener;

import com.solveria.core.accruals.domain.event.QuinquenioPaymentOverdueEvent;
import com.solveria.core.dossier.domain.event.DocumentValidationRejectedEvent;
import com.solveria.core.dossier.domain.event.EligibilitySuspendedByComplianceEvent;
import com.solveria.core.experience.application.port.out.RelationshipPersonResolverPort;
import com.solveria.core.experience.application.usecase.CrossBcEventConsumerUseCase;
import com.solveria.core.security.context.SecurityTenantContext;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Listener de eventos cross-BC para el Bounded Context Experience (BC 6).
 *
 * <p>Responsabilidad única: recibir eventos de dominio de otros BCs (Accruals, Dossier)
 * y delegar la ejecución al {@link CrossBcEventConsumerUseCase}. PROHIBIDO colocar lógica
 * de negocio, cálculos, hardcode o fabricación de datos en esta clase.
 *
 * <p>Los datos que el evento no transporta se resuelven a través de:
 * <ul>
 *   <li>{@link RelationshipPersonResolverPort}: resolución de personId a partir de relationshipId (ACL).</li>
 *   <li>{@link SecurityTenantContext}: resolución del tenantId del contexto de seguridad.</li>
 *   <li>Propiedad {@code experience.prediction.default-model-id}: PredictionModel por defecto.</li>
 * </ul>
 *
 * <p>Eventos consumidos:
 * <ol>
 *   <li>{@link QuinquenioPaymentOverdueEvent} → {@code handleQuinquenioPaymentOverdue}</li>
 *   <li>{@link DocumentValidationRejectedEvent} → {@code handleDocumentValidationRejected}</li>
 *   <li>{@link EligibilitySuspendedByComplianceEvent} → {@code handleEligibilitySuspended}</li>
 * </ol>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CrossBcEventListener {

  // ── Use Case ────────────────────────────────────────────────────────────
  private final CrossBcEventConsumerUseCase crossBcEventConsumerUseCase;

  // ── ACL Port (resolución de datos cross-BC) ─────────────────────────────
  private final RelationshipPersonResolverPort relationshipPersonResolverPort;

  // ── Configuración ───────────────────────────────────────────────────────
  /**
   * ID del PredictionModel por defecto. Se inyecta desde la propiedad
   * {@code experience.prediction.default-model-id} de application.yml.
   * Usado cuando el evento no transporta un modelId explícito.
   */
  @Value("${experience.prediction.default-model-id}")
  private UUID defaultPredictionModelId;

  // ──────────────────────────────────────────────────────────────────────────
  // 1. QuinquenioPaymentOverdueEvent → handleQuinquenioPaymentOverdue
  // ──────────────────────────────────────────────────────────────────────────

  /**
   * Reacciona al vencimiento de pago de quinquenio (Accruals BC).
   *
   * <p>Mapping:
   * <ul>
   *   <li>{@code modelId} → {@link #defaultPredictionModelId} (estático, vía property)</li>
   *   <li>{@code personId} → {@code event.personId()}</li>
   *   <li>{@code amount} → {@code event.penaltyAmount()}</li>
   *   <li>{@code tenantId} → {@link SecurityTenantContext#getCurrentTenantId()} con fallback</li>
   * </ul>
   */
  @EventListener
  public void handle(QuinquenioPaymentOverdueEvent event) {
    log.info(
        "event=EXP_QUINQUENIO_OVERDUE_RECEIVED personId={} provisionId={} amount={}",
        event.personId(),
        event.provisionId(),
        event.penaltyAmount());
    try {
      String tenantId = resolveTenantId();

      crossBcEventConsumerUseCase.handleQuinquenioPaymentOverdue(
          defaultPredictionModelId,
          event.personId(),
          event.penaltyAmount(),
          tenantId);

      log.info(
          "event=EXP_QUINQUENIO_OVERDUE_PROCESSED personId={} modelId={}",
          event.personId(),
          defaultPredictionModelId);
    } catch (Exception ex) {
      log.warn(
          "event=EXP_QUINQUENIO_OVERDUE_FAILED personId={} error={}",
          event.personId(),
          ex.getMessage(),
          ex);
    }
  }

  // ──────────────────────────────────────────────────────────────────────────
  // 2. DocumentValidationRejectedEvent → handleDocumentValidationRejected
  // ──────────────────────────────────────────────────────────────────────────

  /**
   * Reacciona al rechazo de validación de documento (Dossier BC).
   *
   * <p>Mapping:
   * <ul>
   *   <li>{@code personId} → resuelto desde {@code event.relationshipId()} vía ACL port</li>
   *   <li>{@code documentType} → {@code "Documento ID: " + event.docId()}</li>
   *   <li>{@code reason} → {@code event.reason()}</li>
   *   <li>{@code tenantId} → {@link SecurityTenantContext#getCurrentTenantId()} con fallback</li>
   * </ul>
   */
  @EventListener
  public void handle(DocumentValidationRejectedEvent event) {
    log.info(
        "event=EXP_DOC_VALIDATION_REJECTED_RECEIVED docId={} relationshipId={}",
        event.docId(),
        event.relationshipId());
    try {
      UUID personId =
          relationshipPersonResolverPort.resolvePersonIdByRelationship(event.relationshipId());
      String documentType = "Documento ID: " + event.docId();
      String tenantId = resolveTenantId();

      crossBcEventConsumerUseCase.handleDocumentValidationRejected(
          personId,
          documentType,
          event.reason(),
              UUID.fromString(tenantId));

      log.info(
          "event=EXP_DOC_VALIDATION_REJECTED_PROCESSED docId={} personId={}",
          event.docId(),
          personId);
    } catch (Exception ex) {
      log.warn(
          "event=EXP_DOC_VALIDATION_REJECTED_FAILED docId={} error={}",
          event.docId(),
          ex.getMessage(),
          ex);
    }
  }

  // ──────────────────────────────────────────────────────────────────────────
  // 3. EligibilitySuspendedByComplianceEvent → handleEligibilitySuspended
  // ──────────────────────────────────────────────────────────────────────────

  /**
   * Reacciona a la suspensión de elegibilidad por compliance (Dossier BC).
   *
   * <p>Mapping:
   * <ul>
   *   <li>{@code modelId} → {@link #defaultPredictionModelId} (estático/default)</li>
   *   <li>{@code personId} → resuelto desde {@code event.relationshipId()} vía ACL port</li>
   *   <li>{@code reason} → {@code "Documento crítico expirado"} (constante de negocio)</li>
   *   <li>{@code tenantId} → {@link SecurityTenantContext#getCurrentTenantId()} con fallback</li>
   * </ul>
   */
  @EventListener
  public void handle(EligibilitySuspendedByComplianceEvent event) {
    log.info(
        "event=EXP_ELIGIBILITY_SUSPENDED_RECEIVED relationshipId={}",
        event.relationshipId());
    try {
      UUID personId =
          relationshipPersonResolverPort.resolvePersonIdByRelationship(event.relationshipId());
      String tenantId = resolveTenantId();

      crossBcEventConsumerUseCase.handleEligibilitySuspended(
          defaultPredictionModelId,
          personId,
          REASON_CRITICAL_DOCUMENT_EXPIRED,
              UUID.fromString(tenantId));

      log.info(
          "event=EXP_ELIGIBILITY_SUSPENDED_PROCESSED relationshipId={} personId={} modelId={}",
          event.relationshipId(),
          personId,
          defaultPredictionModelId);
    } catch (Exception ex) {
      log.warn(
          "event=EXP_ELIGIBILITY_SUSPENDED_FAILED relationshipId={} error={}",
          event.relationshipId(),
          ex.getMessage(),
          ex);
    }
  }

  // ── Constantes ──────────────────────────────────────────────────────────

  private static final String REASON_CRITICAL_DOCUMENT_EXPIRED = "Documento crítico expirado";

  // ── Métodos auxiliares ──────────────────────────────────────────────────

  /**
   * Resuelve el tenantId del contexto de seguridad actual.
   * Fallback a {@code "UNKNOWN"} si no hay contexto de tenant establecido
   * (p.ej. en procesamiento asíncrono de eventos donde el SecurityContext
   * puede no estar propagado).
   */
  private String resolveTenantId() {
    String tenantId = SecurityTenantContext.getCurrentTenantId();
    if (tenantId == null || tenantId.isBlank()) {
      log.warn("event=EXP_TENANT_RESOLUTION_FALLBACK reason=NO_SECURITY_CONTEXT");
      return "UNKNOWN";
    }
    return tenantId;
  }
}
