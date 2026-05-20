package com.solveria.TimeAndBearings.domain.model.enums;

/**
 * Ciclo de vida del {@code TimesheetPeriod} (Aggregate 16).
 *
 * <p>Transiciones válidas:
 *
 * <pre>
 *  OPEN → IN_GRACE_PERIOD → CLOSING → CLOSED → TRANSMITTED
 *  OPEN → CLOSING → CLOSED → TRANSMITTED   (cierre manual en periodo activo)
 * </pre>
 *
 * <p>P-TM33: Una vez en {@code CLOSED} o {@code TRANSMITTED}, el período es completamente
 * inmutable.
 */
public enum PeriodStatus {

  /**
   * El periodo está dentro del rango activo (period_start ≤ hoy ≤ period_end). Los {@code
   * AttendanceLedger} siguen aceptando marcaciones.
   */
  OPEN,

  /**
   * El periodo de trabajo finalizó pero aún estamos dentro del Periodo de Gracia (period_end < hoy
   * ≤ grace_period_end, P-TM34). Los {@code AttendanceLedger} siguen aceptando modificaciones. No
   * se puede emitir {@code ATTENDANCE_PERIOD_CLOSED} aún.
   */
  IN_GRACE_PERIOD,

  /**
   * El sistema está ejecutando el cierre masivo de ledgers pendientes. Estado transitorio antes de
   * pasar a {@code CLOSED}.
   */
  CLOSING,

  /**
   * Todos los {@code AttendanceLedger} están {@code CLOSED} e inmutables (P-TM33). El {@code
   * PayrollHandoffPackage} ha sido generado.
   */
  CLOSED,

  /**
   * El evento {@code ATTENDANCE_PERIOD_CLOSED} fue publicado al Message Broker con el payload del
   * {@code PayrollHandoffPackage}. Estado terminal.
   */
  TRANSMITTED
}
