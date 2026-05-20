package com.solveria.TimeAndBearings.domain.model.enums;

/**
 * Indica el actor que ejecutó el cierre del {@code TimesheetPeriod} (Aggregate 16).
 *
 * <p>P-TM34: Al día 3 a las 17:00 hora local del Tenant, el CRON ejecuta el cierre masivo ({@code
 * AUTO}) de todos los registros pendientes y emite {@code ATTENDANCE_PERIOD_CLOSED} sin
 * intervención humana adicional.
 */
public enum ClosureType {

  /** Cierre iniciado por un MSS o Analista de Planillas (WF-TM03, paso 6). */
  MANUAL,

  /** Cierre automático ejecutado por el CRON al vencer el Periodo de Gracia (P-TM34). */
  AUTO
}
