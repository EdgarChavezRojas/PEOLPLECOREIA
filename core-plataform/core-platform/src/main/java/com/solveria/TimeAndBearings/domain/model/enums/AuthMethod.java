package com.solveria.TimeAndBearings.domain.model.enums;

/**
 * Método de autenticación empleado en un intento de marcación. Diccionario de Datos BC-TM v1.2 –
 * PunchAttemptLog.auth_method. Correlacionado con los niveles de P-TM29 (Biometric Auth Levels).
 */
public enum AuthMethod {

  /** Huella dactilar. P-TM29 Nivel 1 – Kiosco físico. */
  FINGERPRINT,

  /** Reconocimiento facial con liveness detection. P-TM29 Nivel 2/3 – Canal móvil. */
  FACIAL,

  /** PIN de 6 dígitos. Complementario a FACIAL en P-TM29 Nivel 2/3. */
  PIN,

  /** Tarjeta NFC de proximidad. */
  NFC,

  /** Código QR de sesión de corta duración. */
  QR
}
