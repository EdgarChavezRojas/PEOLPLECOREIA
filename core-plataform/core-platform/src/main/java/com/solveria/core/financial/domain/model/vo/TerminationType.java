package com.solveria.core.financial.domain.model.vo;

/** Enum: Tipo de desvinculación laboral. Determina si aplica Desahucio (3 salarios promedio). */
public enum TerminationType {
  /** Despido sin causa justificada → aplica Desahucio. */
  DESPIDO_SIN_CAUSA,

  /** Renuncia voluntaria del trabajador. */
  RENUNCIA_VOLUNTARIA,

  /** Despido con causa justificada según Art. 16 LGT. */
  DESPIDO_CON_CAUSA,

  /** Fin de contrato a plazo fijo. */
  FIN_CONTRATO_PLAZO_FIJO,

  /** Jubilación del trabajador. */
  JUBILACION,

  /** Fallecimiento del trabajador. */
  FALLECIMIENTO
}
