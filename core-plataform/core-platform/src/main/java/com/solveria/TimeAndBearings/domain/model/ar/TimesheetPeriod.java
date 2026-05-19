package com.solveria.TimeAndBearings.domain.model.ar;

import com.solveria.core.shared.events.DomainEvent;
import com.solveria.TimeAndBearings.domain.event.AttendancePeriodClosedEvent;
import com.solveria.TimeAndBearings.domain.exception.GracePeriodActiveException;
import com.solveria.TimeAndBearings.domain.exception.PendingLedgersBlockClosureException;
import com.solveria.TimeAndBearings.domain.exception.TimesheetPeriodImmutableException;
import com.solveria.TimeAndBearings.domain.model.entity.DailyConsolidationSummary;
import com.solveria.TimeAndBearings.domain.model.entity.PayrollHandoffPackage;
import com.solveria.TimeAndBearings.domain.model.enums.ClosureType;
import com.solveria.TimeAndBearings.domain.model.enums.PeriodStatus;
import com.solveria.TimeAndBearings.domain.model.enums.PeriodType;
import com.solveria.TimeAndBearings.domain.model.vo.EmployeeHandoffRecord;
import com.solveria.TimeAndBearings.domain.model.vo.PeriodBoundary;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root 16: TimesheetPeriod — Periodo de Consolidación.
 *
 * <p>Contenedor de cierre de un periodo de tiempo (WEEKLY, BIWEEKLY, MONTHLY) y
 * gestor de la transmisión formal hacia BC-05 (Financial &amp; Payroll) vía
 * Message Broker. Es el "sobre sellado" que BC-05 recibe.
 *
 * <h2>Responsabilidades clave:</h2>
 * <ul>
 *   <li>Garantizar que el 100% de los {@code AttendanceLedger} del periodo estén
 *       {@code CLOSED} antes de publicar {@code ATTENDANCE_PERIOD_CLOSED} (P-TM33).</li>
 *   <li>Gestionar el Periodo de Gracia (P-TM34): la emisión del evento está bloqueada
 *       hasta que {@code grace_period_end} venza o el MSS ejecute un cierre manual.</li>
 *   <li>Gestionar exclusivamente las entidades {@code DailyConsolidationSummary} y
 *       {@code PayrollHandoffPackage}.</li>
 *   <li>Mantener la inmutabilidad P-TM33 una vez en estado {@code CLOSED/TRANSMITTED}.</li>
 * </ul>
 *
 * <h2>Invariantes del dominio:</h2>
 * <ol>
 *   <li><b>period_end &gt; period_start</b>: Validado en {@link PeriodBoundary}.</li>
 *   <li><b>100% CLOSED antes de emitir evento</b>: Validado en
 *       {@link #closePeriod(UUID, int, List, List, Instant)}
 *       y {@link #autoClosePeriod(int, List, List, Instant)}.</li>
 *   <li><b>Grace Period (P-TM34)</b>: El auto-cierre solo puede ejecutarse si
 *       {@code now &gt; grace_period_end}; validado en
 *       {@link #autoClosePeriod(int, List, List, Instant)}.</li>
 *   <li><b>Inmutabilidad (P-TM33)</b>: Todo intento de modificar un periodo
 *       {@code CLOSED} o {@code TRANSMITTED} lanza
 *       {@link TimesheetPeriodImmutableException}.</li>
 * </ol>
 *
 * <p><b>Dominio puro:</b> Ninguna anotación de Spring ni JPA. Usa Java Records
 * para Value Objects y Java 21 features.
 */
public class TimesheetPeriod {

    private UUID periodId;
    private UUID tenantId;
    private UUID orgUnitId;
    private PeriodBoundary periodBoundary;
    private PeriodStatus status;
    private LocalDateTime closedAt;
    private UUID closedBy;
    private ClosureType closureType;
    private LocalDateTime payrollEventEmittedAt;
    private List<DailyConsolidationSummary> dailySummaries;
    private PayrollHandoffPackage handoffPackage;
    private List<DomainEvent> domainEvents;

    public void setPeriodId(UUID periodId) { this.periodId = periodId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public void setOrgUnitId(UUID orgUnitId) { this.orgUnitId = orgUnitId; }
    public void setPeriodBoundary(PeriodBoundary periodBoundary) { this.periodBoundary = periodBoundary; }
    public void setStatus(PeriodStatus status) { this.status = status; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }
    public void setClosedBy(UUID closedBy) { this.closedBy = closedBy; }
    public void setClosureType(ClosureType closureType) { this.closureType = closureType; }
    public void setPayrollEventEmittedAt(LocalDateTime payrollEventEmittedAt) { this.payrollEventEmittedAt = payrollEventEmittedAt; }
    public void setDailySummaries(List<DailyConsolidationSummary> dailySummaries) { this.dailySummaries = dailySummaries; }
    public void setHandoffPackage(PayrollHandoffPackage handoffPackage) { this.handoffPackage = handoffPackage; }
    public void setDomainEvents(List<DomainEvent> domainEvents) { this.domainEvents = domainEvents; }
    /**
     * Constructor de reconstrucción (desde persistencia / repositorio).
     * Restaura el estado completo del agregado sin disparar eventos.
     */
    public TimesheetPeriod(
            UUID periodId,
            UUID tenantId,
            UUID orgUnitId,
            PeriodBoundary periodBoundary,
            PeriodStatus status,
            LocalDateTime closedAt,
            UUID closedBy,
            ClosureType closureType,
            LocalDateTime payrollEventEmittedAt,
            List<DailyConsolidationSummary> dailySummaries,
            PayrollHandoffPackage handoffPackage) {
        this.periodId = Objects.requireNonNull(periodId, "periodId es requerido");
        this.tenantId = Objects.requireNonNull(tenantId, "tenantId es requerido");
        this.orgUnitId = Objects.requireNonNull(orgUnitId, "orgUnitId es requerido");
        this.periodBoundary = Objects.requireNonNull(periodBoundary, "periodBoundary es requerido");
        this.status = Objects.requireNonNull(status, "status es requerido");
        this.closedAt = closedAt;
        this.closedBy = closedBy;
        this.closureType = closureType;
        this.payrollEventEmittedAt = payrollEventEmittedAt;
        this.dailySummaries = new ArrayList<>(
                Objects.requireNonNullElse(dailySummaries, List.of()));
        this.handoffPackage = handoffPackage;
        this.domainEvents = new ArrayList<>();
    }

    /**
     * Factory de creación de un nuevo {@code TimesheetPeriod} en estado {@code OPEN}.
     *
     * @param tenantId      FK opaca al Tenant (BC-01)
     * @param orgUnitId     FK opaca a la OrgUnit (BC-01)
     * @param periodStart   primer día del periodo
     * @param periodEnd     último día del periodo
     * @param periodType    WEEKLY / BIWEEKLY / MONTHLY
     * @param gracePeriodEnd calculado externamente según P-TM34 (period_end + 3 días hábiles)
     * @return nueva instancia en estado {@code OPEN}
     */
    public static TimesheetPeriod open(
            UUID tenantId,
            UUID orgUnitId,
            LocalDate periodStart,
            LocalDate periodEnd,
            PeriodType periodType,
            LocalDateTime gracePeriodEnd) {
        PeriodBoundary boundary = new PeriodBoundary(periodStart, periodEnd, periodType, gracePeriodEnd);
        return new TimesheetPeriod(
                UUID.randomUUID(),
                tenantId,
                orgUnitId,
                boundary,
                PeriodStatus.OPEN,
                null, null, null, null,
                new ArrayList<>(),
                null);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Comportamiento del dominio
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Transiciona el periodo a {@code IN_GRACE_PERIOD} cuando el periodo de trabajo
     * finalizó pero el {@code grace_period_end} aún no venció (P-TM34).
     *
     * <p>Invocado por el CRON nocturno al detectar que {@code period_end &lt; now ≤ grace_period_end}.
     *
     * @param now timestamp del servidor NTP
     * @throws TimesheetPeriodImmutableException si el periodo ya está CLOSED/TRANSMITTED
     */
    public void enterGracePeriod(LocalDateTime now) {
        guardNotImmutable();
        if (status == PeriodStatus.OPEN && periodBoundary.isInGracePeriod(now)) {
            this.status = PeriodStatus.IN_GRACE_PERIOD;
        }
    }

    /**
     * Ejecuta el cierre MANUAL del periodo (WF-TM03, paso 6), iniciado por un MSS
     * o Analista de Planillas.
     *
     * <p><b>Pre-condición (P-TM33):</b> Todos los {@code AttendanceLedger} del
     * periodo deben estar {@code CLOSED}. Si {@code pendingLedgersCount &gt; 0},
     * lanza {@link PendingLedgersBlockClosureException}.
     *
     * <p>Genera el {@code PayrollHandoffPackage} con checksum SHA-512 y registra
     * el evento {@code ATTENDANCE_PERIOD_CLOSED} para publicación vía Outbox.
     *
     * @param closedBy           UUID del MSS/Analista que ejecuta el cierre
     * @param pendingLedgersCount número de ledgers aún sin cerrar en el periodo
     * @param employeeRecords    lista de registros por empleado calculados por la UC
     * @param dailySummariesToAdd resúmenes diarios calculados por la UC
     * @param serverInstant      timestamp del servidor NTP
     * @throws PendingLedgersBlockClosureException si hay ledgers pendientes
     * @throws TimesheetPeriodImmutableException   si el periodo ya está CLOSED/TRANSMITTED
     */
    public void closePeriod(
            UUID closedBy,
            int pendingLedgersCount,
            List<EmployeeHandoffRecord> employeeRecords,
            List<DailyConsolidationSummary> dailySummariesToAdd,
            Instant serverInstant) {
        guardNotImmutable();
        Objects.requireNonNull(closedBy, "closedBy es requerido para cierre manual");
        guardNoPendingLedgers(pendingLedgersCount);

        LocalDateTime serverNow = LocalDateTime.ofInstant(serverInstant,
                java.time.ZoneOffset.UTC);

        executeClose(
                closedBy,
                ClosureType.MANUAL,
                employeeRecords,
                dailySummariesToAdd,
                serverNow,
                serverInstant);
    }

    /**
     * Ejecuta el cierre AUTOMÁTICO del periodo por vencimiento del Periodo de Gracia
     * (WF-TM03, paso 6 + P-TM34).
     *
     * <p><b>Pre-condición (P-TM34):</b> {@code now} debe ser posterior a
     * {@code grace_period_end}. Si aún está dentro del periodo de gracia,
     * lanza {@link GracePeriodActiveException}.
     *
     * <p><b>Pre-condición (P-TM33):</b> Aunque el auto-cierre aplica cierre masivo,
     * los ledgers ya deben haber sido procesados por la UC antes de llamar aquí.
     * Si {@code pendingLedgersCount &gt; 0} al llegar a este punto, lanza
     * {@link PendingLedgersBlockClosureException}.
     *
     * @param pendingLedgersCount número de ledgers aún sin cerrar (debe ser 0)
     * @param employeeRecords     lista de registros por empleado (puede incluir PARTIAL_AUTO_CLOSED)
     * @param dailySummariesToAdd resúmenes diarios calculados por la UC
     * @param serverInstant       timestamp del servidor NTP
     * @throws GracePeriodActiveException          si el periodo de gracia aún no venció
     * @throws PendingLedgersBlockClosureException  si hay ledgers pendientes
     * @throws TimesheetPeriodImmutableException   si el periodo ya está CLOSED/TRANSMITTED
     */
    public void autoClosePeriod(
            int pendingLedgersCount,
            List<EmployeeHandoffRecord> employeeRecords,
            List<DailyConsolidationSummary> dailySummariesToAdd,
            Instant serverInstant) {
        guardNotImmutable();

        LocalDateTime serverNow = LocalDateTime.ofInstant(serverInstant,
                java.time.ZoneOffset.UTC);

        // P-TM34: el auto-cierre solo puede ejecutarse si grace_period_end venció
        if (!periodBoundary.isGracePeriodExpired(serverNow)) {
            throw new GracePeriodActiveException(periodId, periodBoundary.gracePeriodEnd());
        }

        guardNoPendingLedgers(pendingLedgersCount);

        executeClose(
                null,          // closedBy = null → cierre AUTO
                ClosureType.AUTO,
                employeeRecords,
                dailySummariesToAdd,
                serverNow,
                serverInstant);
    }

    /**
     * Registra que el evento fue publicado exitosamente al Message Broker,
     * transicionando el periodo a {@code TRANSMITTED} (estado terminal).
     *
     * <p>Invocado por la UC después de que el Outbox haya confirmado la publicación.
     *
     * @param publishedAt timestamp del momento de publicación
     * @throws TimesheetPeriodImmutableException si el periodo no está en estado CLOSED
     */
    public void markAsTransmitted(LocalDateTime publishedAt) {
        if (status != PeriodStatus.CLOSED) {
            throw new TimesheetPeriodImmutableException(periodId, status);
        }
        this.status = PeriodStatus.TRANSMITTED;
        this.payrollEventEmittedAt = publishedAt;
    }

    /**
     * Añade o actualiza un {@code DailyConsolidationSummary} para el periodo.
     * Solo puede ser invocado cuando el periodo está en {@code OPEN} o {@code IN_GRACE_PERIOD}.
     *
     * @param summary resumen calculado por el CRON para un work_date específico
     * @throws TimesheetPeriodImmutableException si el periodo está CLOSED/TRANSMITTED
     */
    public void addOrUpdateDailySummary(DailyConsolidationSummary summary) {
        guardNotImmutable();
        Objects.requireNonNull(summary, "summary es requerido");
        dailySummaries.removeIf(s -> s.getWorkDate().equals(summary.getWorkDate()));
        dailySummaries.add(summary);
    }

    /**
     * Devuelve y limpia la lista de eventos de dominio pendientes de publicar.
     * Debe ser invocado por el repositorio/UC después de persistir el agregado.
     *
     * @return copia de los eventos pendientes
     */
    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Guards privados (invariantes)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Invariante P-TM33: el periodo NO puede ser mutado si ya está CLOSED o TRANSMITTED.
     */
    private void guardNotImmutable() {
        if (status == PeriodStatus.CLOSED || status == PeriodStatus.TRANSMITTED) {
            throw new TimesheetPeriodImmutableException(periodId, status);
        }
    }

    /**
     * Invariante P-TM33: el cierre está bloqueado si hay ledgers sin cerrar.
     */
    private void guardNoPendingLedgers(int pendingLedgersCount) {
        if (pendingLedgersCount > 0) {
            throw new PendingLedgersBlockClosureException(periodId, pendingLedgersCount);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Lógica de cierre compartida (MANUAL y AUTO)
    // ─────────────────────────────────────────────────────────────────────────

    private void executeClose(
            UUID closedByActor,
            ClosureType type,
            List<EmployeeHandoffRecord> employeeRecords,
            List<DailyConsolidationSummary> dailySummariesToAdd,
            LocalDateTime serverNow,
            Instant serverInstant) {

        this.status = PeriodStatus.CLOSING;

        // Añadir los resúmenes diarios finales calculados por la UC
        if (dailySummariesToAdd != null) {
            for (DailyConsolidationSummary s : dailySummariesToAdd) {
                dailySummaries.removeIf(existing -> existing.getWorkDate().equals(s.getWorkDate()));
                dailySummaries.add(s);
            }
        }

        // Generar PayrollHandoffPackage inmutable con checksum SHA-512
        this.handoffPackage = PayrollHandoffPackage.create(
                periodId,
                serverNow,
                employeeRecords != null ? employeeRecords : List.of());

        // Registrar el cierre
        this.closedAt = serverNow;
        this.closedBy = closedByActor;
        this.closureType = type;
        this.status = PeriodStatus.CLOSED;

        // Registrar evento de dominio para publicación vía Outbox (WF-TM03, paso 7)
        AttendancePeriodClosedEvent event = AttendancePeriodClosedEvent.of(
                periodId,
                orgUnitId,
                tenantId,
                periodBoundary,
                type,
                handoffPackage,
                serverInstant);
        domainEvents.add(event);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Getters (sin setters públicos: toda mutación pasa por métodos de dominio)
    // ─────────────────────────────────────────────────────────────────────────

    public UUID getPeriodId() { return periodId; }
    public UUID getTenantId() { return tenantId; }
    public UUID getOrgUnitId() { return orgUnitId; }
    public PeriodBoundary getPeriodBoundary() { return periodBoundary; }
    public PeriodStatus getStatus() { return status; }
    public LocalDateTime getClosedAt() { return closedAt; }
    public UUID getClosedBy() { return closedBy; }
    public ClosureType getClosureType() { return closureType; }
    public LocalDateTime getPayrollEventEmittedAt() { return payrollEventEmittedAt; }
    public PayrollHandoffPackage getHandoffPackage() { return handoffPackage; }

    /** @return vista inmutable de los resúmenes diarios. */
    public List<DailyConsolidationSummary> getDailySummaries() {
        return Collections.unmodifiableList(dailySummaries);
    }
}
