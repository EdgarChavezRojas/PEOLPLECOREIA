package com.solveria.core.experience.application.port.in;

import java.util.UUID;

/**
 * Primary Port (Inbound): Employee Self-Service (ESS). Expone operaciones de autoservicio para
 * empleados. W11: Actualización de datos personales. W14: Solicitud de certificados digitales.
 */
public interface EmployeeSelfServicePI {

  /** W11: Solicitar actualización de datos personales. */
  UUID requestDataUpdate(UUID personId, String payload, String tenantId, String createdBy);

  /** W14: Solicitar certificado digital con hash SHA-256 y QR Zero-Trust. */
  UUID requestCertificate(UUID personId, String certificateType, String tenantId, String createdBy);
}
