package com.solveria.TimeAndBearings.application.port.inbound;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Inbound Port: Consolidación y Cierre de Periodo de Timesheet (WF-TM03).
 *
 * <p>Define los casos de uso accesibles desde fuera de la capa de aplicación
 * (Controladores REST, Jobs CRON, consumidores de eventos) para la gestión
 * del ciclo de vida del {@code TimesheetPeriod} (Aggregate 16).
 *
 * <p>La implementación concreta es {@code TimesheetConsolidationUseCase}.
 */
public interface TimesheetConsolidationPort {

    /**
     * Abre un nuevo {@code TimesheetPeriod} para una OrgUnit y un rango de fechas.
     *
     * @param command datos de apertura del periodo
     * @return identificador del periodo creado
     */
    UUID openPeriod(OpenPeriodCommand command);

    /**
     * Ejecuta la consolidación diaria (CRON nocturno, 00:30 AM) para un periodo.
     *
     * <p>Proceso: barrido de AttendanceLedgers → cálculo de WorkedHoursSummary →
     * creación de DailyConsolidationSummary → transición de estados de ledgers
     * (WF-TM03, pasos 1-5).
     *
     * @param periodId identificador del periodo a consolidar
     */
    void runDailyConsolidation(UUID periodId);

    /**
     * Ejecuta el cierre MANUAL del periodo iniciado por un MSS o Analista (WF-TM03, paso 6).
     *
     * <p>Verifica que el 100% de los AttendanceLedger estén CLOSED (P-TM33),
     * genera el PayrollHandoffPackage y emite ATTENDANCE_PERIOD_CLOSED.
     *
     * @param command datos del cierre manual
     */
    void closePeriodManually(ClosePeriodManuallyCommand command);

    /**
     * Evalúa el estado del Periodo de Gracia (P-TM34) para todos los periodos
     * elegibles y ejecuta el auto-cierre masivo si {@code grace_period_end} venció.
     *
     * <p>Invocado por el CRON @Scheduled del {@code TimesheetConsolidationUseCase}.
     * Al día 3 a las 17:00 hora local del Tenant, el CRON aplica el cierre AUTO.
     */
    void evaluateAndExecuteGracePeriodClosure();

    // ── Comandos (records inmutables — Java 21) ───────────────────────────────

    /**
     * Comando para abrir un nuevo {@code TimesheetPeriod}.
     *
     * @param tenantId     FK opaca al Tenant (BC-01)
     * @param orgUnitId    FK opaca a la OrgUnit (BC-01)
     * @param periodStart  primer día del periodo
     * @param periodEnd    último día del periodo
     * @param periodTypeId tipo de periodo: "WEEKLY", "BIWEEKLY" o "MONTHLY"
     */
    record OpenPeriodCommand(
            UUID tenantId,
            UUID orgUnitId,
            LocalDate periodStart,
            LocalDate periodEnd,
            String periodTypeId) {}

    /**
     * Comando para el cierre manual de un periodo por un actor humano.
     *
     * @param periodId  identificador del periodo a cerrar
     * @param closedBy  UUID del MSS o Analista que ejecuta el cierre
     */
    record ClosePeriodManuallyCommand(
            UUID periodId,
            UUID closedBy) {}
}
