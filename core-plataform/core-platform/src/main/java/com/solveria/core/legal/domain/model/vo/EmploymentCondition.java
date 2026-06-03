package com.solveria.core.legal.domain.model.vo;

/**
 * Condición de empleo del trabajador. Estandariza la nomenclatura utilizada en los reportes
 * tabulares de vacaciones y antigüedad.
 *
 * <p>PE = Permanente, PF = Plazo Fijo, JU = Jubilado.
 */
public enum EmploymentCondition {
  /** Permanente: contrato indefinido activo. */
  PE,

  /** Plazo Fijo: contrato con fecha de término definida. */
  PF,

  /** Jubilado: trabajador en condición de jubilación. */
  JU,

  /** Tiempo Completo. */
  TC
}
