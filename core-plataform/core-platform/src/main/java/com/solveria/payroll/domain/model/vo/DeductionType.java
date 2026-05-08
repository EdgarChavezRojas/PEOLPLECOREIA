package com.solveria.payroll.domain.model.vo;

/**
 * Enum: Tipos de egreso/descuento del período.
 *
 * <p>Cada valor corresponde a un concepto que reduce el Total Ganado
 * para obtener el Líquido Pagable (Workflow Fase 3).
 */
public enum DeductionType {

    /** Descuento por atrasos — proporcional al salario diario. */
    ATRASO,

    /** Descuento por ausencia injustificada. */
    AUSENCIA,

    /** Anticipo de salario del período corriente. */
    ANTICIPO,

    /** Cuota de préstamo institucional. */
    CUOTA_PRESTAMO,

    /** Pensión alimenticia por orden judicial. */
    PENSION_ALIMENTICIA,

    /** Seguro complementario de salud (COSSMIL, seguro privado). */
    SEGURO_COMPLEMENTARIO
}
