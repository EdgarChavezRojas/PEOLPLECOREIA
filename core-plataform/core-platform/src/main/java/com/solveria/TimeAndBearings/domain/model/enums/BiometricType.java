package com.solveria.TimeAndBearings.domain.model.enums;

/**
 * Tipo de credencial biométrica almacenada en un BiometricEnrollment.
 * Diccionario de Datos BC-TM v1.2 – BiometricEnrollment.biometric_type.
 * El template raw NUNCA se almacena; solo el hash SHA-512 normalizado.
 */
public enum BiometricType {

    /** Huella dactilar. P-TM29 Nivel 1 (kiosco físico). Mínimo 3 muestras de captura. */
    FINGERPRINT,

    /** Reconocimiento facial con liveness detection. P-TM29 Nivel 2/3 (canal móvil). Mínimo 5 muestras. */
    FACIAL
}
