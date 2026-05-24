package com.solveria.core.experience.application.port.in;

import java.util.UUID;

/**
 * Primary Port (Inbound): Manager Self-Service (MSS). Expone operaciones de aprobación/rechazo para
 * managers. Implementa Invariante SoD: el manager no puede ser el mismo solicitante.
 */
public interface ManagerSelfServicePI {

  /** W11: Aprobar solicitud de cambio de datos. */
  void approveDataChange(UUID actionId, UUID approvedBy, String tenantId);

  /** W11: Rechazar solicitud de cambio de datos. */
  void rejectDataChange(UUID actionId, UUID rejectedBy, String rejectionReason, String tenantId);
}
