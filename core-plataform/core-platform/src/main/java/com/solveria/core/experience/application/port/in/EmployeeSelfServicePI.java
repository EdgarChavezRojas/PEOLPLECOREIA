package com.solveria.core.experience.application.port.in;

import com.solveria.core.experience.application.command.RequestDataUpdateCommand;
import com.solveria.core.experience.application.command.RequestLeaveCommand;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Primary Port (Inbound): Employee Self-Service (ESS). Expone operaciones de autoservicio para
 * empleados. W11: Actualización de datos personales. W12: Acuse de recibo de memorandos. W14:
 * Solicitud de certificados digitales. Leave: Solicitud de ausencias/permisos.
 */
public interface EmployeeSelfServicePI {

  /** W11: Solicitar actualización de datos personales. */
  UUID requestDataUpdate(RequestDataUpdateCommand cmd);

  /** W14: Solicitar certificado digital con hash SHA-256 y QR Zero-Trust. */
  UUID requestCertificate(UUID personId, String certificateType, UUID tenantId, String createdBy);

  /**
   * Cancelar una solicitud ESS pendiente de revisión. Solo el solicitante original puede cancelar.
   */
  void cancelDataUpdate(UUID actionId, UUID personId, UUID tenantId);

  /** W12: Acuse de recibo formal de memorando/notificación. */
  void acknowledgeNotification(UUID notificationId, UUID personId);

  /** Solicitar ausencia/permiso vía ESS. */
  UUID requestLeave(RequestLeaveCommand cmd);

  /** Consultar saldo disponible de vacaciones/ausencias para un empleado. */
  BigDecimal getAvailableLeaveBalance();
}
