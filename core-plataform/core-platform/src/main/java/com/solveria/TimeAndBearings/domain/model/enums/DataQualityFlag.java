package com.solveria.TimeAndBearings.domain.model.enums;

/**
 * Indicador de calidad de datos del {@code EmployeeHandoffRecord} (VO de Aggregate 16).
 *
 * <p>Permite que BC-05 (Payroll) diferencie los registros con datos completos de aquellos donde el
 * sistema aplicó auto-cierre masivo por vencimiento de la ventana de justificación (P-TM31 /
 * P-TM34).
 */
public enum DataQualityFlag {

  /**
   * Todos los {@code AttendanceLedger} del empleado en el periodo fueron cerrados y validados sin
   * auto-cierres masivos. Datos de alta confianza.
   */
  COMPLETE,

  /**
   * Al menos un {@code AttendanceLedger} del empleado fue cerrado automáticamente por vencimiento
   * de la ventana de justificación (P-TM31 / P-TM34). BC-05 debe aplicar la regla de descuento por
   * ausencia injustificada.
   */
  PARTIAL_AUTO_CLOSED
}
