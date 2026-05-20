package com.solveria.core.experience.domain.model;

import com.solveria.core.experience.domain.event.CertificateGeneratedEvent;
import com.solveria.core.experience.domain.event.DataChangeCancelledEvent;
import com.solveria.core.experience.domain.event.DataChangeRejectedEvent;
import com.solveria.core.experience.domain.event.DataChangeRequestedEvent;
import com.solveria.core.experience.domain.event.LeaveRequestedViaEssEvent;
import com.solveria.core.experience.domain.model.vo.ActionType;
import com.solveria.core.experience.domain.model.vo.CertificatePayload;
import com.solveria.core.shared.outbox.domain.DomainRoot;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Aggregate Root: SelfServiceAction. Representa una acción de autoservicio (ESS) iniciada por un
 * empleado.
 *
 * <p>Invariante SoD (Segregación de Funciones): - Ninguna solicitud con impacto financiero/data se
 * auto-aprueba. - Requiere validación de un nivel superior (MSS).
 *
 * <p>Workflows asociados: - W11: Actualización de Datos Personales (DATA_UPDATE). - W14:
 * Certificaciones y Constancias Digitales (CERTIFICATE_REQUEST).
 *
 * <p>Dominio puro: SIN anotaciones de infraestructura.
 */
public class SelfServiceAction extends DomainRoot {

  private UUID actionId;
  private UUID personId;
  private ActionType actionType;
  private String payload;
  private UUID tenantId;
  private String createdBy;
  private Instant createdAt;

  /** Flujo de aprobación embebido (Entity). */
  private ApprovalWorkflow approvalWorkflow;

  /** Payload de certificado generado (solo para CERTIFICATE_REQUEST). */
  private CertificatePayload certificatePayload;

  /** Constructor privado: factory methods obligatorias. */
  private SelfServiceAction() {}

  // ─── Factory Methods ───────────────────────────────────────────

  /**
   * W11: Solicitud de actualización de datos personales (ESS). Crea la acción y su flujo de
   * aprobación en estado PENDING_REVIEW. Emite DATA_CHANGE_REQUESTED.
   *
   * @param personId ID del empleado solicitante
   * @param payload JSON con datos a modificar (dirección, banco, etc.)
   * @param tenantId Tenant del empleado
   * @param createdBy ID del usuario que crea la solicitud (debe ser el mismo personId para ESS)
   */
  public static SelfServiceAction requestDataUpdate(
      UUID personId, String payload, UUID tenantId, String createdBy) {
    validateCommonArgs(personId, tenantId, createdBy);
    if (payload == null || payload.isBlank()) {
      throw new IllegalArgumentException("El payload de datos a modificar no puede estar vacío");
    }

    SelfServiceAction action = new SelfServiceAction();
    action.actionId = UUID.randomUUID();
    action.personId = personId;
    action.actionType = ActionType.DATA_UPDATE;
    action.payload = payload;
    action.tenantId = tenantId;
    action.createdBy = createdBy;
    action.createdAt = Instant.now();

    // Invariante SoD: siempre inicia con aprobación pendiente
    action.approvalWorkflow = ApprovalWorkflow.initiate(action.actionId, createdBy);

    action.registerEvent(
        new DataChangeRequestedEvent(
            action.actionId, personId, ActionType.DATA_UPDATE.name(), payload, tenantId));

    return action;
  }

  /**
   * W14: Solicitud de certificado/constancia digital (ESS). Genera el certificado y emite
   * CERTIFICATE_GENERATED.
   *
   * @param personId ID del empleado solicitante
   * @param certificatePayload Payload del certificado con hash SHA-256 y QR
   * @param tenantId Tenant del empleado
   * @param createdBy ID del usuario creador
   */
  public static SelfServiceAction requestCertificate(
      UUID personId, CertificatePayload certificatePayload, UUID tenantId, String createdBy) {
    validateCommonArgs(personId, tenantId, createdBy);
    if (certificatePayload == null) {
      throw new IllegalArgumentException("El payload del certificado es obligatorio");
    }

    SelfServiceAction action = new SelfServiceAction();
    action.actionId = UUID.randomUUID();
    action.personId = personId;
    action.actionType = ActionType.CERTIFICATE_REQUEST;
    action.payload = "{\"certificateType\":\"" + certificatePayload.certificateType() + "\"}";
    action.tenantId = tenantId;
    action.createdBy = createdBy;
    action.createdAt = Instant.now();
    action.certificatePayload = certificatePayload;

    // W14: Certificados no requieren aprobación MSS (autoservicio directo)
    action.approvalWorkflow = null;

    action.registerEvent(
        new CertificateGeneratedEvent(
            action.actionId,
            personId,
            certificatePayload.certificateType(),
            certificatePayload.sha256Hash(),
            certificatePayload.qrValidationUrl(),
            tenantId));

    return action;
  }

  /**
   * Solicitud de ausencia/permiso vía ESS. Crea la acción como LEAVE_REQUEST con flujo de
   * aprobación PENDING_REVIEW y emite LeaveRequestedViaEssEvent.
   *
   * @param personId ID del empleado solicitante
   * @param leaveType Tipo de ausencia (VACACION, PERMISO, etc.)
   * @param startDate Fecha inicio de la ausencia
   * @param endDate Fecha fin de la ausencia
   * @param tenantId Tenant del empleado
   * @param createdBy ID del usuario que crea la solicitud
   */
  public static SelfServiceAction requestLeave(
      UUID personId,
      String leaveType,
      LocalDate startDate,
      LocalDate endDate,
      UUID tenantId,
      String createdBy) {
    validateCommonArgs(personId, tenantId, createdBy);
    if (leaveType == null || leaveType.isBlank()) {
      throw new IllegalArgumentException("El tipo de ausencia no puede estar vacío");
    }
    if (startDate == null || endDate == null) {
      throw new IllegalArgumentException("Las fechas de inicio y fin son obligatorias");
    }
    if (endDate.isBefore(startDate)) {
      throw new IllegalArgumentException("La fecha fin no puede ser anterior a la fecha inicio");
    }

    SelfServiceAction action = new SelfServiceAction();
    action.actionId = UUID.randomUUID();
    action.personId = personId;
    action.actionType = ActionType.LEAVE_REQUEST;
    action.payload =
        "{\"leaveType\":\""
            + leaveType
            + "\",\"startDate\":\""
            + startDate
            + "\",\"endDate\":\""
            + endDate
            + "\"}";
    action.tenantId = tenantId;
    action.createdBy = createdBy;
    action.createdAt = Instant.now();

    // Leave requests requieren aprobación MSS (invariante SoD)
    action.approvalWorkflow = ApprovalWorkflow.initiate(action.actionId, createdBy);

    action.registerEvent(
        new LeaveRequestedViaEssEvent(
            action.actionId, personId, leaveType, startDate, endDate, tenantId));

    return action;
  }

  // ─── Behavior Methods ──────────────────────────────────────────

  /**
   * W11: Aprobación MSS de la solicitud de cambio de datos. Invariante SoD: el aprobador NO puede
   * ser el mismo solicitante.
   *
   * @param approvedBy ID del manager que aprueba (MSS)
   */
  public void approveDataChange(UUID approvedBy) {
    if (this.actionType != ActionType.DATA_UPDATE) {
      throw new IllegalStateException("Solo solicitudes DATA_UPDATE pueden aprobarse vía MSS");
    }
    if (this.approvalWorkflow == null) {
      throw new IllegalStateException("Esta acción no tiene flujo de aprobación");
    }
    // Invariante SoD estricta: no auto-aprobación
    if (approvedBy.toString().equals(this.createdBy)) {
      throw new IllegalStateException(
          "Invariante SoD violada: el solicitante no puede aprobar su propia solicitud");
    }
    this.approvalWorkflow.approve(approvedBy);
  }

  /**
   * W11: Rechazo MSS de la solicitud de cambio de datos. Emite DATA_CHANGE_REJECTED.
   *
   * @param rejectedBy ID del manager que rechaza (MSS)
   * @param rejectionReason Motivo del rechazo
   */
  public void rejectDataChange(UUID rejectedBy, String rejectionReason) {
    if (this.actionType != ActionType.DATA_UPDATE) {
      throw new IllegalStateException("Solo solicitudes DATA_UPDATE pueden rechazarse vía MSS");
    }
    if (this.approvalWorkflow == null) {
      throw new IllegalStateException("Esta acción no tiene flujo de aprobación");
    }
    if (rejectedBy.toString().equals(this.createdBy)) {
      throw new IllegalStateException(
          "Invariante SoD violada: el solicitante no puede rechazar su propia solicitud");
    }
    this.approvalWorkflow.reject(rejectedBy, rejectionReason);

    this.registerEvent(
        new DataChangeRejectedEvent(
            this.actionId, this.personId, rejectionReason, rejectedBy, this.tenantId));
  }

  /**
   * Cancela una solicitud ESS pendiente de revisión. Solo el autor original puede cancelar su
   * propia solicitud.
   *
   * <p>Invariante: Solo acciones en estado PENDING_REVIEW pueden cancelarse. Invariante: Solo el
   * solicitante original (personId) puede cancelar.
   *
   * @param requestingPersonId ID del empleado que solicita la cancelación
   */
  public void cancel(UUID requestingPersonId) {
    if (this.approvalWorkflow == null) {
      throw new IllegalStateException("Esta acción no tiene flujo de aprobación cancelable");
    }
    if (!this.personId.equals(requestingPersonId)) {
      throw new IllegalStateException(
          "Solo el solicitante original puede cancelar su propia solicitud");
    }
    this.approvalWorkflow.cancel(requestingPersonId.toString());

    this.registerEvent(new DataChangeCancelledEvent(this.actionId, this.personId, this.tenantId));
  }

  // ─── Rehydration (from persistence) ────────────────────────────

  /**
   * Rehidrata un SelfServiceAction desde persistencia. NO emite eventos de dominio (son solo para
   * operaciones de negocio).
   */
  public static SelfServiceAction rehydrate(
      UUID actionId,
      UUID personId,
      ActionType actionType,
      String payload,
      UUID tenantId,
      String createdBy,
      Instant createdAt,
      ApprovalWorkflow approvalWorkflow,
      CertificatePayload certificatePayload) {
    SelfServiceAction action = new SelfServiceAction();
    action.actionId = actionId;
    action.personId = personId;
    action.actionType = actionType;
    action.payload = payload;
    action.tenantId = tenantId;
    action.createdBy = createdBy;
    action.createdAt = createdAt;
    action.approvalWorkflow = approvalWorkflow;
    action.certificatePayload = certificatePayload;
    return action;
  }

  // ─── Event Handling ────────────────────────────────────────────

  // ─── Getters (pure domain, no Lombok) ──────────────────────────

  public UUID getActionId() {
    return actionId;
  }

  public UUID getPersonId() {
    return personId;
  }

  public ActionType getActionType() {
    return actionType;
  }

  public String getPayload() {
    return payload;
  }

  public UUID getTenantId() {
    return tenantId;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public ApprovalWorkflow getApprovalWorkflow() {
    return approvalWorkflow;
  }

  public CertificatePayload getCertificatePayload() {
    return certificatePayload;
  }

  // ─── Private Helpers ───────────────────────────────────────────

  private static void validateCommonArgs(UUID personId, UUID tenantId, String createdBy) {
    if (personId == null) {
      throw new IllegalArgumentException("personId no puede ser nulo");
    }

    if (createdBy == null || createdBy.isBlank()) {
      throw new IllegalArgumentException("createdBy no puede estar vacío");
    }
  }
}
