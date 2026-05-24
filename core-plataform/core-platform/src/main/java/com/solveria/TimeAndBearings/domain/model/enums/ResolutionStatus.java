package com.solveria.TimeAndBearings.domain.model.enums;

/**
 * Estado del ciclo de vida de un TimeDeviationRecord. Definido en Diccionario de Datos BC-TM v1.2 –
 * TimeDeviationRecord.resolution_status.
 */
public enum ResolutionStatus {

  /** Excepción creada, pendiente de acción del MSS. Dentro de la ventana P-TM31. */
  PENDING,

  /**
   * Excepción aprobada (ej. overtime aprobado). reason_note obligatorio (mínimo 20 caracteres según
   * P-TM32).
   */
  APPROVED,

  /**
   * Excepción rechazada (ej. overtime descartado; se ajusta al expected_end teórico). reason_note
   * obligatorio.
   */
  REJECTED,

  /**
   * El MSS corrigió la marcación manualmente. Para MISSING_PUNCH o GEO_VIOLATION: TimeEntry de
   * corrección creado con source=MANUAL. Libera la Invariante Attendance Closure Parity para ese
   * hueco.
   */
  OVERRIDDEN_BY_MANAGER,

  /**
   * Ventana P-TM31 vencida sin resolución. Registro en ExceptionAuditLog con actor=SYSTEM,
   * reason=WINDOW_EXPIRED. Ausencias: descuento de día completo sin reversión. Overtime:
   * descartado.
   */
  AUTO_CLOSED_AS_UNJUSTIFIED
}
