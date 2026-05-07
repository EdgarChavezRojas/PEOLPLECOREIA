package com.solveria.TimeAndBearings.domain.model.enums;

/**
 * Categoría del evento de marcación.
 * Definido en Diccionario de Datos BC-TM v1.2 – TimeEntry.punch_type.
 */
public enum PunchType {

    /** Inicio de jornada. Invariante: solo puede existir uno activo sin PUNCH_OUT. */
    PUNCH_IN,

    /** Fin de jornada. Debe ser cronológicamente posterior a su PUNCH_IN (Invariante Chronological Integrity). */
    PUNCH_OUT,

    /** Inicio de pausa / descanso. */
    MEAL_START,

    /** Fin de pausa / descanso. */
    MEAL_END,

    /**
     * Corrección manual generada por el MSS. Referencia al entry original via
     * TimeEntry.correctsEntryId (P-TM32).
     */
    MANUAL_CORRECTION,

    /**
     * Marcación recibida fuera de la ventana de idempotencia de 5 min pero con el
     * mismo punch_type (P-TM27). Requiere revisión del MSS.
     */
    DUPLICATE_REVIEW
}
