package com.solveria.core.financial.application.port;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Primary Port: Social Security Compliance. Expone operaciones de gestión de cuentas de seguridad
 * social y aportes a la Gestora.
 */
public interface SocialSecurityCompliancePort {

  /** Registra una nueva cuenta de seguridad social (NUA/CUA). */
  UUID registerSocialSecurityAccount(
      UUID personId, String gestoraCode, String tenantId, String createdBy);

  /** Calcula la deducción de la Gestora para un trabajador. */
  BigDecimal calculateGestoraDeduction(UUID ssaId, BigDecimal totalGanado);

  /** Actualiza la fecha del último aporte. */
  void updateLastContribution(UUID ssaId, LocalDate contributionDate);

  /** Registra un proveedor de salud (Caja Nacional, etc.). */
  UUID registerHealthProvider(String registrationNo, String tenantId, String createdBy);

  /** Suspende un proveedor de salud. */
  void suspendHealthProvider(UUID providerId);

  /** Activa un proveedor de salud. */
  void activateHealthProvider(UUID providerId);
}
