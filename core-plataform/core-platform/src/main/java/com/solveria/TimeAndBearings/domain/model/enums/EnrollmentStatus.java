package com.solveria.TimeAndBearings.domain.model.enums;

/**
 * Estado del ciclo de vida de un BiometricEnrollment.
 * Diccionario de Datos BC-TM v1.2 – BiometricEnrollment.status.
 * WF-TM04: Flujo de Enrolamiento Biométrico y Baja/Revocación.
 */
public enum EnrollmentStatus {

    /** El template biométrico está activo y autoriza marcaciones en el dispositivo. */
    ACTIVE,

    /** Suspendido temporalmente (no autoriza pero preserva el template). */
    SUSPENDED,

    /**
     * Revocado de forma permanente (WF-TM04 Flujo Baja).
     * El dispositivo debe eliminar el template local en el próximo heartbeat.
     * Todo intento posterior genera PunchAttemptLog con security_incident=TRUE (P-TM30).
     */
    REVOKED
}
