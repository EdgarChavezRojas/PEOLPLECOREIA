package com.solveria.core.experience.domain.model;

import com.solveria.core.experience.domain.model.vo.ApprovalStatus;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Entity: ApprovalWorkflow. Pertenece al agregado SelfServiceAction. Gestiona el ciclo de vida de
 * aprobación de solicitudes ESS.
 *
 * <p>Invariante SoD (Segregación de Funciones): - El campo history registra quién y cuándo actuó en
 * cada paso. - No permite auto-aprobación.
 *
 * <p>Dominio puro: SIN anotaciones de infraestructura.
 */
public class ApprovalWorkflow {

  private UUID workflowId;
  private UUID actionId;
  private int currentStep;
  private ApprovalStatus status;
  private List<ApprovalHistoryEntry> history;
  private Instant createdAt;

  private ApprovalWorkflow() {
    this.history = new ArrayList<>();
  }

  // ─── Factory Methods ───────────────────────────────────────────

  /**
   * Inicia un nuevo flujo de aprobación para una acción ESS. Siempre comienza en PENDING_REVIEW,
   * paso 1.
   */
  public static ApprovalWorkflow initiate(UUID actionId, String initiatedBy) {
    ApprovalWorkflow wf = new ApprovalWorkflow();
    wf.workflowId = UUID.randomUUID();
    wf.actionId = actionId;
    wf.currentStep = 1;
    wf.status = ApprovalStatus.PENDING_REVIEW;
    wf.createdAt = Instant.now();

    wf.history.add(
        new ApprovalHistoryEntry(
            initiatedBy, "INITIATED", "Solicitud creada vía ESS", Instant.now()));

    return wf;
  }

  // ─── Behavior ──────────────────────────────────────────────────

  /** Aprueba la solicitud (MSS). Registra en el historial con timestamp para auditoría. */
  public void approve(UUID approvedBy) {
    if (this.status != ApprovalStatus.PENDING_REVIEW) {
      throw new IllegalStateException(
          "Solo solicitudes en PENDING_REVIEW pueden aprobarse. Estado actual: " + this.status);
    }
    this.status = ApprovalStatus.APPROVED;
    this.currentStep++;
    this.history.add(
        new ApprovalHistoryEntry(
            approvedBy.toString(), "APPROVED", "Aprobado por MSS", Instant.now()));
  }

  /** Rechaza la solicitud (MSS). Registra motivo y actor en el historial. */
  public void reject(UUID rejectedBy, String reason) {
    if (this.status != ApprovalStatus.PENDING_REVIEW) {
      throw new IllegalStateException(
          "Solo solicitudes en PENDING_REVIEW pueden rechazarse. Estado actual: " + this.status);
    }
    this.status = ApprovalStatus.REJECTED;
    this.history.add(
        new ApprovalHistoryEntry(rejectedBy.toString(), "REJECTED", reason, Instant.now()));
  }

  /**
   * Cancela la solicitud (ESS). Solo solicitudes en PENDING_REVIEW pueden cancelarse. Registra el
   * actor que cancela en el historial para auditoría.
   *
   * @param cancelledBy ID del empleado que cancela (debe ser el solicitante original)
   */
  public void cancel(String cancelledBy) {
    if (this.status != ApprovalStatus.PENDING_REVIEW) {
      throw new IllegalStateException(
          "Solo solicitudes en PENDING_REVIEW pueden cancelarse. Estado actual: " + this.status);
    }
    this.status = ApprovalStatus.CANCELLED;
    this.history.add(
        new ApprovalHistoryEntry(
            cancelledBy, "CANCELLED", "Cancelado por el solicitante vía ESS", Instant.now()));
  }

  // ─── Rehydration ───────────────────────────────────────────────

  public static ApprovalWorkflow rehydrate(
      UUID workflowId,
      UUID actionId,
      int currentStep,
      ApprovalStatus status,
      List<ApprovalHistoryEntry> history,
      Instant createdAt) {
    ApprovalWorkflow wf = new ApprovalWorkflow();
    wf.workflowId = workflowId;
    wf.actionId = actionId;
    wf.currentStep = currentStep;
    wf.status = status;
    wf.history = history != null ? new ArrayList<>(history) : new ArrayList<>();
    wf.createdAt = createdAt;
    return wf;
  }

  // ─── Getters ───────────────────────────────────────────────────

  public UUID getWorkflowId() {
    return workflowId;
  }

  public UUID getActionId() {
    return actionId;
  }

  public int getCurrentStep() {
    return currentStep;
  }

  public ApprovalStatus getStatus() {
    return status;
  }

  public List<ApprovalHistoryEntry> getHistory() {
    return Collections.unmodifiableList(history);
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  // ─── Inner VO: History Entry ───────────────────────────────────

  /**
   * Registro inmutable del historial de aprobación. Invariante SoD: Quién/Cuándo para auditoría.
   */
  public record ApprovalHistoryEntry(
      String actorId, String action, String comment, Instant timestamp) {}
}
