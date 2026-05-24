package com.solveria.TimeAndBearings.domain.model.enums;

/**
 * Canal de origen de la marcación. Definido en Diccionario de Datos BC-TM v1.2 – TimeEntry.source.
 * Determina el nivel de autenticación requerido (P-TM29) y la validación de firma digital
 * (Invariante Device Signature Integrity).
 */
public enum PunchSource {

  /** App ESS del colaborador (móvil). Requiere PIN + Biometría facial (P-TM29 Nivel 2/3). */
  MOBILE,

  /** Kiosco físico en sitio. Requiere huella dactilar (P-TM29 Nivel 1). */
  KIOSK,

  /**
   * Lector biométrico dedicado. Firma digital obligatoria (Invariante Device Signature Integrity).
   */
  BIOMETRIC_READER,

  /** Portal web MSS. Requiere 2FA del MSS (P-TM29 Nivel 4). */
  WEB,

  /**
   * Marcación retroactiva o corrección creada manualmente por el MSS (P-TM32). is_retroactive =
   * TRUE, device_signature = NULL.
   */
  MANUAL
}
