package com.solveria.TimeAndBearings.domain.model.enums;

/**
 * Tipo de periodo de consolidación del timesheet (P-TM34 / Aggregate 16).
 *
 * <p>Configurado a nivel de Tenant según la frecuencia de pago de nómina.
 */
public enum PeriodType {

  /** Periodo de 7 días naturales. */
  WEEKLY,

  /** Periodo de 15 días naturales (quincenal). */
  BIWEEKLY,

  /** Periodo del mes calendario completo. */
  MONTHLY
}
