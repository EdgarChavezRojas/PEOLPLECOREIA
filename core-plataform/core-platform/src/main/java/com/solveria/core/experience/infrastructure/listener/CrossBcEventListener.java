package com.solveria.core.experience.infrastructure.listener;

import com.solveria.core.accruals.domain.event.LeaveRequestSubmittedEvent;
import com.solveria.core.accruals.domain.event.QuinquenioPaymentOverdueEvent;
import com.solveria.core.dossier.domain.event.DocumentValidationRejectedEvent;
import com.solveria.core.dossier.domain.event.EligibilitySuspendedByComplianceEvent;
import com.solveria.core.experience.application.port.out.PredictionModelPO;
import com.solveria.core.experience.application.port.out.RelationshipPersonResolverPort;
import com.solveria.core.experience.application.usecase.CrossBcEventConsumerUseCase;
import com.solveria.core.experience.application.usecase.SendNotificationUseCase;
import com.solveria.core.experience.domain.model.PredictionModel;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Listener de eventos cross-BC para el Bounded Context Experience (BC 6).
 *
 * <p>Responsabilidad única: recibir eventos de dominio de otros BCs (Accruals, Dossier) y delegar
 * la ejecución al {@link CrossBcEventConsumerUseCase}. PROHIBIDO colocar lógica de negocio,
 * cálculos, hardcode o fabricación de datos en esta clase.
 *
 * <p>Los datos que el evento no transporta se resuelven a través de:
 *
 * <ul>
 *   <li>{@link RelationshipPersonResolverPort}: resolución de personId a partir de relationshipId
 *       (ACL).
 *   <li>Eventos Autocontenidos: El tenantId ahora viaja dentro de cada evento, asegurando su
 *       trazabilidad asíncrona sin depender de contextos de hilo (ThreadLocal).
 *   <li>{@link PredictionModelPO}: Búsqueda dinámica del modelo de IA correspondiente al tenant,
 *       garantizando la separación de datos entre clientes.
 * </ul>
 *
 * <p>Eventos consumidos:
 *
 * <ol>
 *   <li>{@link QuinquenioPaymentOverdueEvent} → {@code handleQuinquenioPaymentOverdue}
 *   <li>{@link DocumentValidationRejectedEvent} → {@code handleDocumentValidationRejected}
 *   <li>{@link EligibilitySuspendedByComplianceEvent} → {@code handleEligibilitySuspended}
 * </ol>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CrossBcEventListener {

  // ── Use Cases ────────────────────────────────────────────────────────────
  private final CrossBcEventConsumerUseCase crossBcEventConsumerUseCase;
  private final SendNotificationUseCase sendNotificationUseCase;

  // ── ACL Port (resolución de datos cross-BC) ─────────────────────────────
  private final RelationshipPersonResolverPort relationshipPersonResolverPort;

  // ── Outbound Ports ──────────────────────────────────────────────────────
  // Nuevo puerto integrado para obtener dinámicamente el modelo predictivo del tenant
  // reemplazando la constante hardcodeada anterior (defaultPredictionModelId).
  private final PredictionModelPO predictionModelPO;

  // ──────────────────────────────────────────────────────────────────────────
  // 1. QuinquenioPaymentOverdueEvent → handleQuinquenioPaymentOverdue
  // ──────────────────────────────────────────────────────────────────────────

  /**
   * Reacciona al vencimiento de pago de quinquenio (Accruals BC).
   *
   * <p>Mapping:
   *
   * <ul>
   *   <li>{@code modelId} → Resuelto dinámicamente desde BD mediante el tenantId
   *   <li>{@code personId} → {@code event.personId()}
   *   <li>{@code amount} → {@code event.penaltyAmount()}
   *   <li>{@code tenantId} → {@code event.tenantId()} obtenido desde el propio evento
   * </ul>
   */
  @EventListener
  public void handle(QuinquenioPaymentOverdueEvent event) {
    log.info(
        "event=EXP_QUINQUENIO_OVERDUE_RECEIVED personId={} provisionId={} amount={} tenantId={}",
        event.personId(),
        event.provisionId(),
        event.penaltyAmount(),
        event.tenantId());
    try {
      UUID tenantId = event.tenantId();

      // Buscamos dinámicamente el modelo predictivo que le pertenece a ESTE tenant.
      // Si un tenant no tiene modelo configurado, fallamos rápido para mantener integridad de
      // negocio.
      PredictionModel model =
          predictionModelPO
              .findByTenantId(tenantId)
              .orElseThrow(
                  () ->
                      new IllegalStateException(
                          "No se encontró PredictionModel activo para el tenant: " + tenantId));

      crossBcEventConsumerUseCase.handleQuinquenioPaymentOverdue(
          model.getModelId(), event.personId(), event.penaltyAmount());

      log.info(
          "event=EXP_QUINQUENIO_OVERDUE_PROCESSED personId={} modelId={}",
          event.personId(),
          model.getModelId());
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
   *
   * <ul>
   *   <li>{@code personId} → resuelto desde {@code event.relationshipId()} vía ACL port
   *   <li>{@code documentType} → {@code "Documento ID: " + event.docId()}
   *   <li>{@code reason} → {@code event.reason()}
   *   <li>{@code tenantId} → {@code event.tenantId()} obtenido desde el propio evento
   * </ul>
   */
  @EventListener
  public void handle(DocumentValidationRejectedEvent event) {
    log.info(
        "event=EXP_DOC_VALIDATION_REJECTED_RECEIVED docId={} relationshipId={} tenantId={}",
        event.docId(),
        event.relationshipId(),
        event.tenantId());
    try {
      UUID personId =
          relationshipPersonResolverPort.resolvePersonIdByRelationship(event.relationshipId());
      String documentType = "Documento ID: " + event.docId();
      UUID tenantId = event.tenantId();

      crossBcEventConsumerUseCase.handleDocumentValidationRejected(
          personId, documentType, event.reason(), tenantId);

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
   *
   * <ul>
   *   <li>{@code modelId} → Resuelto dinámicamente desde BD mediante el tenantId
   *   <li>{@code personId} → resuelto desde {@code event.relationshipId()} vía ACL port
   *   <li>{@code reason} → {@code "Documento crítico expirado"} (constante de negocio)
   *   <li>{@code tenantId} → {@code event.tenantId()} obtenido desde el propio evento
   * </ul>
   */
  @EventListener
  public void handle(EligibilitySuspendedByComplianceEvent event) {
    log.info(
        "event=EXP_ELIGIBILITY_SUSPENDED_RECEIVED relationshipId={} tenantId={}",
        event.relationshipId(),
        event.tenantId());
    try {
      UUID personId =
          relationshipPersonResolverPort.resolvePersonIdByRelationship(event.relationshipId());
      UUID tenantId = event.tenantId();

      // Resolvemos el ID del modelo dinámicamente para procesar la suspensión en el contexto IA
      PredictionModel model =
          predictionModelPO
              .findByTenantId(tenantId)
              .orElseThrow(
                  () ->
                      new IllegalStateException(
                          "No se encontró PredictionModel activo para el tenant: " + tenantId));

      crossBcEventConsumerUseCase.handleEligibilitySuspended(
          model.getModelId(), personId, REASON_CRITICAL_DOCUMENT_EXPIRED);

      log.info(
          "event=EXP_ELIGIBILITY_SUSPENDED_PROCESSED relationshipId={} personId={} modelId={}",
          event.relationshipId(),
          personId,
          model.getModelId());
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

  // ──────────────────────────────────────────────────────────────────────────────
  // 4. LeaveRequestSubmittedEvent → Notificación al Manager
  // ──────────────────────────────────────────────────────────────────────────────

  /**
   * Reacciona a la solicitud de ausencia enviada (Accruals BC). Envía una notificación al
   * supervisor indicando que hay una solicitud de ausencia pendiente de revisión.
   */
  @EventListener
  @Transactional
  public void handle(LeaveRequestSubmittedEvent event) {
    log.info(
        "event=EXP_LEAVE_REQUEST_SUBMITTED_RECEIVED balanceId={} transactionId={} startDate={} endDate={} tenantId={}",
        event.balanceId(),
        event.transactionId(),
        event.startDate(),
        event.endDate(),
        event.tenantId());
    try {
      // Extraemos el tenant directamente sin depender del SecurityTenantContext (hilo asíncrono)
      UUID tenantId = event.tenantId();

      sendNotificationUseCase.send(
          event.balanceId(), // recipientId – se resuelve al supervisor en capas internas
          "PUSH_MOBILE",
          "Solicitud de ausencia pendiente",
          String.format(
              "Se ha recibido una solicitud de ausencia (transactionId=%s) del %s al %s por %s días. Requiere su aprobación.",
              event.transactionId(), event.startDate(), event.endDate(), event.chargeableDays()),
          tenantId);

      log.info(
          "event=EXP_LEAVE_REQUEST_NOTIFICATION_SENT balanceId={} transactionId={}",
          event.balanceId(),
          event.transactionId());
    } catch (Exception ex) {
      log.warn(
          "event=EXP_LEAVE_REQUEST_NOTIFICATION_FAILED transactionId={} error={}",
          event.transactionId(),
          ex.getMessage(),
          ex);
    }
  }
}
