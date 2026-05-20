package com.solveria.TimeAndBearings.domain.model.enums;

/**
 * Razón de revocación de un BiometricEnrollment. Diccionario de Datos BC-TM v1.2 –
 * BiometricEnrollment.revocation_reason. NULL cuando status != REVOKED.
 */
public enum RevocationReason {

  /** Colaborador desvinculado. Trigger: evento EMPLOYEE_DEACTIVATED de BC-01 Core (WF-TM04). */
  EMPLOYEE_OFFBOARDING,

  /** Fraude detectado. Trigger: P-TM30 Proxy Clocking Detection. */
  FRAUD_DETECTED,

  /** Solicitud del propio colaborador. */
  EMPLOYEE_REQUEST,

  /**
   * El dispositivo fue dado de baja (DeviceStatus.DECOMMISSIONED). Todos sus enrollments se
   * revocan.
   */
  DEVICE_DECOMMISSION
}
