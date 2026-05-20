package com.solveria.payroll.application.port.inbound;

import com.solveria.TimeAndBearings.domain.event.AttendancePeriodClosedEvent;

/**
 * Puerto Primario (Inbound): Caso de uso de recepción del handoff de asistencia.
 *
 * <p>Define el contrato para procesar el evento {@code ATTENDANCE_PERIOD_CLOSED} emitido por el
 * módulo TimeAndBearings (BC-TM) al cerrar un período.
 *
 * <p>Workflow Paso 2: "El sistema recibe el resumen validado de horas por empleado y genera los
 * {@code IncomeRecord} (horas extra, recargos) y {@code DeductionRecord} (atrasos, ausencias)
 * correspondientes."
 *
 * <p><b>Regla crítica:</b> Payroll no usa datos de Scheduling directamente. La fuente de verdad de
 * horas trabajadas es siempre TM.
 */
public interface AttendanceHandoffUseCase {

  /**
   * Procesa el evento de cierre de período de asistencia.
   *
   * <p>Por cada empleado en el payload {@code PayrollHandoffPackage}, genera los registros de
   * ingresos y egresos automáticos y los persiste a través de los puertos de repositorio.
   *
   * @param event evento de dominio con el handoff de asistencia
   */
  void handle(AttendancePeriodClosedEvent event);
}
