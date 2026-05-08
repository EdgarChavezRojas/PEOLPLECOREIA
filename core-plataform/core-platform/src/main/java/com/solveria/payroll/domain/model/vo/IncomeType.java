package com.solveria.payroll.domain.model.vo;

/**
 * Enum: Tipos de ingreso del período.
 *
 * <p>Cada valor corresponde a un concepto positivo que suma al Total Ganado
 * del empleado (Workflow Fase 2).
 */
public enum IncomeType {

    /** Horas extra diurnas — recargo 100% sobre costo-hora. */
    HORAS_EXTRA,

    /** Comisiones por ventas — tenant Retail. */
    COMISION,

    /** Recargo por trabajo en domingo o feriado — 100% adicional. */
    RECARGO_DOMINICAL,

    /** Bono de antigüedad — escalonado según años de servicio. */
    BONO_ANTIGUEDAD,

    /** Quinquenio — 60 meses de antigüedad ininterrumpida. */
    QUINQUENIO,

    /** Viáticos sujetos a rendición — ONG, imputables a proyecto. */
    VIATICO_PROYECTO
}
