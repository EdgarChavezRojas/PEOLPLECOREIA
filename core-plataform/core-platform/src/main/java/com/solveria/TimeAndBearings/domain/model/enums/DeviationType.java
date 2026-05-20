package com.solveria.TimeAndBearings.domain.model.enums;

/**
 * Tipo de desviación temporal respecto al turno planificado. Definido en Diccionario de Datos BC-TM
 * v1.2 – TimeDeviationRecord.deviation_type. Prioridad de resolución en MSS: NO_SHOW > OVERTIME >
 * GEO_VIOLATION > LATE_IN (WF-TM02).
 */
public enum DeviationType {

  /** Entrada tardía: punch_time de PUNCH_IN supera el expected_start del AssignedShift (P-TM26). */
  LATE_IN,

  /** Salida anticipada: punch_time de PUNCH_OUT es anterior al expected_end del AssignedShift. */
  EARLY_OUT,

  /**
   * Horas extra: PUNCH_OUT supera el expected_end del AssignedShift (P-TM27). Requiere aprobación
   * del MSS.
   */
  OVERTIME,

  /**
   * Sin marcación alguna en la jornada programada. Ventana de resolución 24h en día de cierre
   * (P-TM31).
   */
  NO_SHOW,

  /**
   * Coordenadas GPS fuera del departamento org_extension_snapshot (P-TM28). TimeEntry persiste con
   * geo_status=OUTSIDE_FENCE; excepción es asíncrona (Non-Blocking Design).
   */
  GEO_VIOLATION,

  /**
   * Par PUNCH_IN / PUNCH_OUT incompleto detectado por el CRON (WF-TM03). Estado
   * OVERRIDDEN_BY_MANAGER libera la Invariante Attendance Closure Parity.
   */
  MISSING_PUNCH,

  /** Ausencia sin justificación válida. Impacta descuento en nómina vía BC-05 (P-TM31). */
  UNAUTHORIZED_ABSENCE
}
