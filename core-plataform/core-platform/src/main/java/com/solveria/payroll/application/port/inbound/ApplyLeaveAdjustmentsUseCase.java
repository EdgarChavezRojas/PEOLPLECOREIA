package com.solveria.payroll.application.port.inbound;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Puerto Primario (Inbound): Aplica ajustes de nómina derivados de solicitudes de ausencia/vacación
 * aprobadas.
 *
 * <p>Este caso de uso es invocado desde el Bounded Context Financial cuando se recibe un evento de
 * aprobación gerencial de vacaciones ({@code LeaveRequestManagerApprovedEvent}).
 */
public interface ApplyLeaveAdjustmentsUseCase {

  /**
   * Aplica los ajustes (deducciones) correspondientes a los días de vacación aprobados.
   *
   * @param transactionId identificador de la transacción de ausencia
   * @param daysRequested cantidad de días solicitados
   * @param tenantId identificador del tenant
   */
  void applyAdjustments(UUID transactionId, BigDecimal daysRequested, UUID tenantId);
}
