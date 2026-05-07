package com.solveria.TimeAndBearings.domain.model.ar;

import com.solveria.TimeAndBearings.domain.event.ExceptionAutoClosedEvent;
import com.solveria.TimeAndBearings.domain.event.PunchAnomalyDetectedEvent;
import com.solveria.TimeAndBearings.domain.event.TimeDeviationJustifiedEvent;
import com.solveria.TimeAndBearings.domain.exception.ActivePunchAlreadyExistsException;
import com.solveria.TimeAndBearings.domain.exception.AttendanceClosureParityException;
import com.solveria.TimeAndBearings.domain.exception.ChronologicalIntegrityException;
import com.solveria.TimeAndBearings.domain.exception.ClosedRecordMutationException;
import com.solveria.TimeAndBearings.domain.exception.PeriodLockedException;
import com.solveria.TimeAndBearings.domain.model.entity.TimeDeviationRecord;
import com.solveria.TimeAndBearings.domain.model.entity.TimeEntry;
import com.solveria.TimeAndBearings.domain.model.enums.DeviationType;
import com.solveria.TimeAndBearings.domain.model.enums.GeoStatus;
import com.solveria.TimeAndBearings.domain.model.enums.LedgerStatus;
import com.solveria.TimeAndBearings.domain.model.enums.PunchType;
import com.solveria.TimeAndBearings.domain.model.enums.ResolutionStatus;
import com.solveria.TimeAndBearings.domain.model.vo.GeoValidationSnapshot;
import com.solveria.TimeAndBearings.domain.model.vo.PunchContext;
import com.solveria.TimeAndBearings.domain.model.vo.WorkedHoursSummary;
import com.solveria.core.shared.events.DomainEvent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Aggregate Root 14: AttendanceLedger (Libro Mayor de Asistencia).
 *
 * <p>Contenedor diario de todos los eventos de marcación de un colaborador.
 * Fuente de verdad para el cálculo de nómina. Puro Java 21 — sin anotaciones Spring/JPA.
 *
 * <h3>Invariantes Enforced</h3>
 * <ol>
 *   <li><b>Active Punch Uniqueness:</b> Solo un PUNCH_IN activo sin PUNCH_OUT por ledger.</li>
 *   <li><b>Chronological Integrity:</b> punch_time ≤ serverNtpNow + 5s; PUNCH_OUT &gt; PUNCH_IN.</li>
 *   <li><b>Attendance Closure Parity:</b> No CLOSED con PUNCHes impares sin OVERRIDDEN_BY_MANAGER MISSING_PUNCH.</li>
 *   <li><b>Finalized Record Immutability:</b> is_finalized=TRUE → ningún campo hijo es modificable (P-TM33).</li>
 * </ol>
 *
 * <h3>Non-Blocking Design (WF-TM01)</h3>
 * Geo o Auth failures NO bloquean el dispositivo. El TimeEntry se persiste con
 * {@code geo_status=OUTSIDE_FENCE} y un {@code TimeDeviationRecord} se crea asincrónicamente.
 * El evento {@code PunchAnomalyDetectedEvent} se publica al Message Broker.
 */
public class AttendanceLedger {

    // ── NTP time tolerance (seconds) ──────────────────────────────────────────
    private static final int NTP_TOLERANCE_SECONDS = 5;

    // ── Identity ───────────────────────────────────────────────────────────────
    private final UUID ledgerId;
    private final UUID tenantId;

    /** Opaque reference to BC-01 Core. BC-TM never reads Person or Contract fields. */
    private final UUID relationshipId;

    private final LocalDate workDate;

    /** FK a AssignedShift (BC-SCH). NULL if no shift assigned (guardia). */
    private final UUID shiftId;

    // ── State ─────────────────────────────────────────────────────────────────
    private LedgerStatus status;
    private boolean finalized;
    private boolean remoteWork;
    private UUID remoteWorkAuthId;
    private final LocalDateTime createdAt;
    private LocalDateTime closedAt;

    // ── Children ──────────────────────────────────────────────────────────────
    private final List<TimeEntry> timeEntries = new ArrayList<>();
    private final List<TimeDeviationRecord> deviations = new ArrayList<>();
    private WorkedHoursSummary workedHoursSummary;

    // ── Domain Events (transient, cleared after flush) ─────────────────────────
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    // ── Constructor (reconstitution + creation) ───────────────────────────────

    /** Factory for creating a brand-new ledger (WF-TM01 step: Ledger not found → create). */
    public static AttendanceLedger open(
            UUID tenantId,
            UUID relationshipId,
            LocalDate workDate,
            UUID shiftId,
            LocalDateTime serverNtpNow) {

        return new AttendanceLedger(
                UUID.randomUUID(), tenantId, relationshipId, workDate, shiftId,
                LedgerStatus.OPEN, false, false, null, serverNtpNow, null);
    }

    /** Reconstitution constructor (used by repository adapter). */
    public AttendanceLedger(
            UUID ledgerId,
            UUID tenantId,
            UUID relationshipId,
            LocalDate workDate,
            UUID shiftId,
            LedgerStatus status,
            boolean finalized,
            boolean remoteWork,
            UUID remoteWorkAuthId,
            LocalDateTime createdAt,
            LocalDateTime closedAt) {

        this.ledgerId = ledgerId;
        this.tenantId = tenantId;
        this.relationshipId = relationshipId;
        this.workDate = workDate;
        this.shiftId = shiftId;
        this.status = status;
        this.finalized = finalized;
        this.remoteWork = remoteWork;
        this.remoteWorkAuthId = remoteWorkAuthId;
        this.createdAt = createdAt;
        this.closedAt = closedAt;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  WF-TM01 — Real-Time Clocking
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Registra un nuevo evento de marcación (WF-TM01 steps 5–8).
     *
     * <p><b>Non-Blocking Design:</b> GEO_VIOLATION no bloquea al colaborador ni al dispositivo.
     * El TimeEntry se persiste con {@code geo_status=OUTSIDE_FENCE} y el AR registra
     * un {@code PunchAnomalyDetectedEvent} para publicación asíncrona (paso 8).
     * El {@code TimeDeviationRecord} de GEO_VIOLATION se genera por el UC (no aquí) para
     * desacoplar la persistencia de la lógica de anomalía.
     *
     * @param serverNtpNow  Hora del servidor NTP. La app cliente NUNCA transmite su propio timestamp (P-TM26).
     * @param punchType     Determinado por el motor en el paso 5 del WF-TM01.
     * @param punchContext  Contexto inmutable del canal y dispositivo.
     * @param geoSnapshot   Resultado del chequeo geográfico Extension-Based (P-TM28).
     * @param deviceSignature Firma del dispositivo (NOT NULL para KIOSK/BIOMETRIC_READER).
     * @param fraudFlag     TRUE si el motor anti-fraude detectó proxy clocking (P-TM30).
     * @return El TimeEntry creado (immutable append).
     */
    public TimeEntry recordPunch(
            LocalDateTime serverNtpNow,
            PunchType punchType,
            PunchContext punchContext,
            GeoValidationSnapshot geoSnapshot,
            String deviceSignature,
            boolean fraudFlag) {

        // Invariante: Finalized Record Immutability (P-TM33)
        assertNotFinalized("recordPunch");

        // Chronological Integrity – Regla B: no future punch_time (P-TM26)
        assertNtpCompliance(serverNtpNow);

        // Active Punch Uniqueness: enforce single active PUNCH_IN
        if (punchType == PunchType.PUNCH_IN) {
            long activePunchIns = timeEntries.stream()
                    .filter(e -> e.getPunchType() == PunchType.PUNCH_IN)
                    .filter(e -> timeEntries.stream()
                            .noneMatch(out -> out.getPunchType() == PunchType.PUNCH_OUT
                                    && out.getPunchTime().isAfter(e.getPunchTime())))
                    .count();
            if (activePunchIns > 0) {
                throw new ActivePunchAlreadyExistsException(ledgerId, relationshipId);
            }
        }

        // Chronological Integrity – Regla A: PUNCH_OUT must be after last PUNCH_IN
        if (punchType == PunchType.PUNCH_OUT || punchType == PunchType.MEAL_START) {
            timeEntries.stream()
                    .filter(e -> e.getPunchType() == PunchType.PUNCH_IN)
                    .max((a, b) -> a.getPunchTime().compareTo(b.getPunchTime()))
                    .ifPresent(lastIn -> {
                        if (!serverNtpNow.isAfter(lastIn.getPunchTime())) {
                            throw new ChronologicalIntegrityException(ledgerId, serverNtpNow,
                                    "Rule A: PUNCH_OUT/MEAL_START must be strictly after the last PUNCH_IN at " +
                                    lastIn.getPunchTime());
                        }
                    });
        }

        TimeEntry entry = new TimeEntry(
                UUID.randomUUID(), ledgerId, serverNtpNow, punchType,
                punchContext, geoSnapshot, deviceSignature,
                false, null, null, fraudFlag);

        timeEntries.add(entry);

        // Transition ledger to PENDING_REVIEW if geo violation detected (Non-Blocking, P-TM28)
        if (geoSnapshot != null && geoSnapshot.geoStatus() == GeoStatus.OUTSIDE_FENCE) {
            this.status = LedgerStatus.PENDING_REVIEW;
            domainEvents.add(new PunchAnomalyDetectedEvent(
                    ledgerId, relationshipId, DeviationType.GEO_VIOLATION,
                    serverNtpNow.toInstant(ZoneOffset.UTC), tenantId));
        }

        return entry;
    }

    /**
     * Crea una marcación retroactiva por el MSS (WF-TM02 / P-TM32).
     *
     * @param serverNtpNow         Hora del servidor NTP al momento de la corrección.
     * @param retroactivePunchTime Hora real de la marcación (pasada).
     * @param punchType            Tipo de marcación corregida.
     * @param mssActorId           UUID del MSS que aprueba (P-TM32).
     * @param secondaryApproverId  Requerido si retroactividad &gt;48h (P-TM32).
     * @param punchContext         Contexto del canal MSS (source=MANUAL).
     * @param correctsEntryId      UUID del TimeEntry original que se corrige (si aplica).
     * @return El TimeEntry retroactivo creado.
     */
    public TimeEntry recordRetroactivePunch(
            LocalDateTime serverNtpNow,
            LocalDateTime retroactivePunchTime,
            PunchType punchType,
            UUID mssActorId,
            UUID secondaryApproverId,
            PunchContext punchContext,
            UUID correctsEntryId) {

        // P-TM32: only OPEN or PENDING_REVIEW ledgers accept retroactive entries
        if (this.status == LedgerStatus.CLOSED || this.finalized) {
            throw new PeriodLockedException(ledgerId);
        }
        assertNotFinalized("recordRetroactivePunch");
        assertNtpCompliance(serverNtpNow);

        // Retroactive time must not be in the future
        if (retroactivePunchTime.isAfter(serverNtpNow.plusSeconds(NTP_TOLERANCE_SECONDS))) {
            throw new ChronologicalIntegrityException(ledgerId, retroactivePunchTime,
                    "Rule B: retroactivePunchTime cannot be in the future relative to server NTP.");
        }

        TimeEntry entry = new TimeEntry(
                UUID.randomUUID(), ledgerId, retroactivePunchTime, punchType,
                punchContext, GeoValidationSnapshot.noGps(),
                null, true, mssActorId, correctsEntryId, false);

        timeEntries.add(entry);
        return entry;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  WF-TM01 Step 7 — Deviation Registration (async, non-blocking)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Registra una desviación detectada de forma asíncrona (Non-Blocking Design).
     * Llamado por el UC después de que el TimeEntry ya fue persistido.
     *
     * @param deviationType    Tipo de desviación detectada.
     * @param deviationMinutes Magnitud en minutos (negativo para salida anticipada).
     * @param detectedAt       Momento NTP de detección.
     * @return El TimeDeviationRecord creado en estado PENDING.
     */
    public TimeDeviationRecord registerDeviation(
            DeviationType deviationType,
            int deviationMinutes,
            LocalDateTime detectedAt) {

        assertNotFinalized("registerDeviation");

        TimeDeviationRecord record = new TimeDeviationRecord(
                UUID.randomUUID(), ledgerId, deviationType, deviationMinutes, detectedAt);
        deviations.add(record);

        if (this.status == LedgerStatus.OPEN) {
            this.status = LedgerStatus.PENDING_REVIEW;
        }

        // Emit domain event for LATE_IN (WF-TM01 step 8)
        if (deviationType == DeviationType.LATE_IN) {
            domainEvents.add(new PunchAnomalyDetectedEvent(
                    ledgerId, relationshipId, DeviationType.LATE_IN,
                    detectedAt.toInstant(ZoneOffset.UTC), tenantId));
        }

        return record;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  WF-TM02 — Exception Handling
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Resuelve una excepción existente (WF-TM02 steps 3–5).
     *
     * @param deviationId      UUID de la desviación a resolver.
     * @param actorId          UUID del MSS o Analista.
     * @param newStatus        Estado resultante.
     * @param reasonNote       Nota obligatoria (min 20 chars, P-TM32).
     * @param secondaryApprover UUID del segundo nivel si aplica.
     * @param resolvedAt       Momento NTP de resolución.
     */
    public void resolveDeviation(
            UUID deviationId,
            UUID actorId,
            ResolutionStatus newStatus,
            String reasonNote,
            UUID secondaryApprover,
            LocalDateTime resolvedAt) {

        assertNotFinalized("resolveDeviation");

        TimeDeviationRecord record = findDeviationOrThrow(deviationId);
        record.resolve(actorId, newStatus, reasonNote, secondaryApprover, resolvedAt);

        // Emit TimeDeviationJustifiedEvent for downstream consumers (BC-01 Notifications)
        domainEvents.add(TimeDeviationJustifiedEvent.of(
                ledgerId,
                deviationId,
                actorId,
                newStatus,
                resolvedAt.toInstant(ZoneOffset.UTC),
                tenantId));

        // If all deviations resolved, allow transition back to OPEN (MSS cleared all)
        boolean anyPending = deviations.stream().anyMatch(TimeDeviationRecord::isPending);
        if (!anyPending && this.status == LedgerStatus.PENDING_REVIEW) {
            this.status = LedgerStatus.OPEN;
        }
    }

    /**
     * Auto-cierre de excepción por vencimiento P-TM31.
     * Emite ExceptionAutoClosedEvent hacia BC-01 Core (Notificaciones).
     *
     * @param deviationId UUID de la desviación.
     * @param closedAt    Momento del auto-cierre (NTP).
     * @param financialImpact Descripción del impacto en nómina.
     */
    public void autoCloseDeviation(UUID deviationId, LocalDateTime closedAt, String financialImpact) {
        TimeDeviationRecord record = findDeviationOrThrow(deviationId);
        record.autoClose(closedAt);

        domainEvents.add(new ExceptionAutoClosedEvent(
                deviationId, relationshipId, ResolutionStatus.AUTO_CLOSED_AS_UNJUSTIFIED,
                "WINDOW_EXPIRED", financialImpact,
                closedAt.toInstant(ZoneOffset.UTC), tenantId));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  WF-TM03 — Consolidation & Closure
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Establece el WorkedHoursSummary calculado por el CRON (WF-TM03 step 3).
     * Solo permitido si el Ledger no está finalizado.
     *
     * @param summary Resultado calculado del CRON.
     */
    public void setWorkedHoursSummary(WorkedHoursSummary summary) {
        assertNotFinalized("setWorkedHoursSummary");
        if (summary == null) throw new IllegalArgumentException("WorkedHoursSummary cannot be null.");
        this.workedHoursSummary = summary;
    }

    /**
     * Transiciona el Ledger a CLOSED con is_finalized=TRUE (WF-TM03 steps 5–6).
     *
     * <p>Enforces:
     * <ul>
     *   <li>Attendance Closure Parity: no PUNCHes impares sin cobertura de OVERRIDDEN_BY_MANAGER.</li>
     *   <li>No deviations in PENDING state.</li>
     *   <li>WorkedHoursSummary must be present.</li>
     * </ul>
     *
     * @param closedAt Momento NTP del cierre.
     */
    public void close(LocalDateTime closedAt) {
        assertNotFinalized("close");

        if (this.workedHoursSummary == null) {
            throw new IllegalStateException(
                    "AttendanceLedger [" + ledgerId + "] cannot be closed without a WorkedHoursSummary.");
        }

        // Attendance Closure Parity invariant
        long uncoveredOpenPunches = countUncoveredOpenPunches();
        if (uncoveredOpenPunches > 0) {
            throw new AttendanceClosureParityException(ledgerId, (int) uncoveredOpenPunches);
        }

        // No pending deviations
        long pendingDeviations = deviations.stream().filter(TimeDeviationRecord::isPending).count();
        if (pendingDeviations > 0) {
            throw new IllegalStateException(
                    "AttendanceLedger [" + ledgerId + "] has " + pendingDeviations +
                    " PENDING deviation(s). Resolve them before closing (WF-TM02).");
        }

        this.status = LedgerStatus.CLOSED;
        this.finalized = true;
        this.closedAt = closedAt;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  Authorization for remote work
    // ═══════════════════════════════════════════════════════════════════════════

    /** Marks the ledger day as authorized remote work (WF-TM05). */
    public void authorizeRemoteWork(UUID remoteWorkAuthId) {
        assertNotFinalized("authorizeRemoteWork");
        this.remoteWork = true;
        this.remoteWorkAuthId = remoteWorkAuthId;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  Domain Events
    // ═══════════════════════════════════════════════════════════════════════════

    /** Returns accumulated domain events and clears the internal list (transactional outbox pattern). */
    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return Collections.unmodifiableList(events);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  Internal helpers
    // ═══════════════════════════════════════════════════════════════════════════

    private void assertNotFinalized(String operation) {
        if (this.finalized) {
            throw new ClosedRecordMutationException(ledgerId, operation);
        }
    }

    private void assertNtpCompliance(LocalDateTime serverNtpNow) {
        // Rule B: punch_time must not be in the future (tolerance ±5s)
        if (serverNtpNow.isAfter(LocalDateTime.now(ZoneOffset.UTC).plusSeconds(NTP_TOLERANCE_SECONDS))) {
            throw new ChronologicalIntegrityException(ledgerId, serverNtpNow,
                    "Rule B: punch_time is in the future beyond the NTP tolerance of " +
                    NTP_TOLERANCE_SECONDS + " seconds (P-TM26).");
        }
    }

    /**
     * Counts PUNCH_IN entries that do not have a subsequent PUNCH_OUT AND are not covered
     * by an OVERRIDDEN_BY_MANAGER MISSING_PUNCH deviation.
     */
    private long countUncoveredOpenPunches() {
        long openPunchIns = timeEntries.stream()
                .filter(e -> e.getPunchType() == PunchType.PUNCH_IN)
                .filter(e -> timeEntries.stream()
                        .noneMatch(out -> out.getPunchType() == PunchType.PUNCH_OUT
                                && out.getPunchTime().isAfter(e.getPunchTime())))
                .count();

        long coveredByOverride = deviations.stream()
                .filter(TimeDeviationRecord::coversOpenPunchGap)
                .count();

        return Math.max(0, openPunchIns - coveredByOverride);
    }

    private TimeDeviationRecord findDeviationOrThrow(UUID deviationId) {
        return deviations.stream()
                .filter(d -> d.getDeviationId().equals(deviationId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "TimeDeviationRecord [" + deviationId + "] not found in ledger [" + ledgerId + "]."));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  Getters (read-only views)
    // ═══════════════════════════════════════════════════════════════════════════

    public UUID getLedgerId() { return ledgerId; }
    public UUID getTenantId() { return tenantId; }
    public UUID getRelationshipId() { return relationshipId; }
    public LocalDate getWorkDate() { return workDate; }
    public UUID getShiftId() { return shiftId; }
    public LedgerStatus getStatus() { return status; }
    public boolean isFinalized() { return finalized; }
    public boolean isRemoteWork() { return remoteWork; }
    public UUID getRemoteWorkAuthId() { return remoteWorkAuthId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getClosedAt() { return closedAt; }
    public List<TimeEntry> getTimeEntries() { return Collections.unmodifiableList(timeEntries); }
    public List<TimeDeviationRecord> getDeviations() { return Collections.unmodifiableList(deviations); }
    public WorkedHoursSummary getWorkedHoursSummary() { return workedHoursSummary; }

    /** Reconstitution: loads time entries (called by repository adapter). */
    public void loadTimeEntries(List<TimeEntry> entries) { this.timeEntries.addAll(entries); }

    /** Reconstitution: loads deviations (called by repository adapter). */
    public void loadDeviations(List<TimeDeviationRecord> devs) { this.deviations.addAll(devs); }
}
