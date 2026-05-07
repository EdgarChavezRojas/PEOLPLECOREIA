package com.solveria.core.experience.domain.model.vo;

/**
 * Estado del flujo de aprobación para solicitudes ESS. Implementa el ciclo de vida de la solicitud
 * según SoD.
 */
public enum ApprovalStatus {
  /** Solicitud creada, pendiente de revisión MSS. */
  PENDING_REVIEW,
  /** Aprobada por nivel superior (MSS). */
  APPROVED,
  /** Rechazada por nivel superior (MSS). */
  REJECTED,
  /** Cancelada por el solicitante. */
  CANCELLED
}
