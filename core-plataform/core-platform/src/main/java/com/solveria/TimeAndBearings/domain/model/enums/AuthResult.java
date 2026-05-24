package com.solveria.TimeAndBearings.domain.model.enums;

/**
 * Resultado de un intento de autenticación en PunchAttemptLog. Diccionario de Datos BC-TM v1.2 –
 * PunchAttemptLog.auth_result. Resultados adversos disparan security_incident=TRUE según P-TM30.
 */
public enum AuthResult {

  /** Autenticación exitosa. El TimeEntry fue creado. */
  SUCCESS,

  /** Fallo en la validación biométrica (huella o facial no coincide). */
  BIOMETRIC_FAIL,

  /** El BiometricEnrollment del colaborador está REVOKED. El intento es un security_incident. */
  REVOKED_CREDENTIAL,

  /**
   * La firma digital del dispositivo no pudo ser verificada. Invariante Device Signature Integrity.
   */
  DEVICE_SIGNATURE_FAIL,

  /**
   * Motor P-TM30 detectó proxy clocking (la biometría corresponde a otro colaborador). Genera
   * SecurityPunchIncidentEvent con nivel CRITICAL. El dispositivo NO se bloquea.
   */
  FRAUD_DETECTED
}
