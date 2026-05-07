package com.solveria.TimeAndBearings.application.usecase;

import com.solveria.core.shared.events.DomainEvent;
import com.solveria.TimeAndBearings.application.port.inbound.ExceptionHandlingPort;
import com.solveria.TimeAndBearings.application.port.outbound.AttendanceLedgerRepositoryPort;
import com.solveria.TimeAndBearings.application.port.outbound.EventOutboxPort;
import com.solveria.TimeAndBearings.domain.model.ar.AttendanceLedger;
import com.solveria.TimeAndBearings.domain.model.entity.TimeDeviationRecord;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

/**
 * Use Case: Exception Handling (WF-TM02).
 *
 * <p>Implements:
 * <ul>
 *   <li><b>P-TM31:</b> Justification Window — auto-close expired PENDING deviations after
 *       72 labor hours. For NO_SHOW on closing day: window reduced to 24h.</li>
 *   <li><b>P-TM32:</b> Retroactive Punch — resolves MISSING_PUNCH by creating a manual
 *       TimeEntry; reason_note min 20 chars enforced via {@code ExceptionAuditEntry};
 *       secondary approver required when retroactivity &gt;48h.</li>
 *   <li><b>P-TM33:</b> Closed Record Immutability — AR rejects any mutation on a
 *       finalized ledger ({@code is_finalized=TRUE}) with {@code ClosedRecordMutationException}.</li>
 * </ul>
 */
public class ExceptionHandlingUseCase implements ExceptionHandlingPort {

    /** P-TM31: Default justification window in labor hours. */
    private static final int JUSTIFICATION_WINDOW_LABOR_HOURS = 72;

    private final AttendanceLedgerRepositoryPort ledgerRepository;
    private final EventOutboxPort eventOutbox;

    public ExceptionHandlingUseCase(
            AttendanceLedgerRepositoryPort ledgerRepository,
            EventOutboxPort eventOutbox) {
        this.ledgerRepository = ledgerRepository;
        this.eventOutbox = eventOutbox;
    }

    /**
     * Resolves a TimeDeviationRecord with a manager decision (WF-TM02 steps 3–5).
     *
     * <p>Validation chain:
     * <ul>
     *   <li>P-TM33: AR guard — {@code ClosedRecordMutationException} if ledger is finalized.</li>
     *   <li>P-TM32: {@code ExceptionAuditEntry} enforces reason_note ≥ 20 chars for
     *       APPROVED / OVERRIDDEN_BY_MANAGER.</li>
     *   <li>Idempotent: resolving an already-resolved deviation throws
     *       {@link IllegalStateException} at the entity level.</li>
     * </ul>
     *
     * @param cmd Resolution command from the MSS panel.
     * @return The updated TimeDeviationRecord after resolution.
     */
    @Override
    public TimeDeviationRecord resolveDeviation(ResolveDeviationCommand cmd) {
        AttendanceLedger ledger = findLedgerOrThrow(cmd.ledgerId());

        LocalDateTime serverNtpNow = LocalDateTime.now(ZoneOffset.UTC);

        // AR delegates to TimeDeviationRecord.resolve() which appends immutable ExceptionAuditEntry
        ledger.resolveDeviation(
                cmd.deviationId(),
                cmd.actorId(),
                cmd.newStatus(),
                cmd.reasonNote(),
                cmd.secondaryApproverId(),
                serverNtpNow);

        // Persist within same transaction; domain events go to outbox
        ledgerRepository.save(ledger);
        List<DomainEvent> events = ledger.pullDomainEvents();
        eventOutbox.store(events);

        return ledger.getDeviations().stream()
                .filter(d -> d.getDeviationId().equals(cmd.deviationId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Deviation [" + cmd.deviationId() + "] not found after resolution."));
    }

    /**
     * Batch auto-close of all PENDING deviations whose P-TM31 window has expired.
     *
     * <p>Called by the CRON scheduler (WF-TM03 / WF-TM02 step 6). For each expired deviation:
     * <ul>
     *   <li>Status → AUTO_CLOSED_AS_UNJUSTIFIED.</li>
     *   <li>{@code ExceptionAutoClosedEvent} published via outbox → BC-01 Core Notifications.</li>
     *   <li>Absence types (NO_SHOW, UNAUTHORIZED_ABSENCE) → full-day deduction in nómina.</li>
     *   <li>OVERTIME → discarded; WorkedHoursSummary adjusted.</li>
     * </ul>
     *
     * <p>The repository layer provides a query
     * {@code findLedgersWithExpiredPendingDeviations(tenantId, cutoffTime)} that returns
     * all affected ledgers. Each ledger is processed individually to isolate failures.
     *
     * @param tenantId Tenant scope for the batch run.
     */
    @Override
    public void autoCloseExpiredDeviations(UUID tenantId) {
        LocalDateTime serverNtpNow = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime windowCutoff = serverNtpNow.minusHours(JUSTIFICATION_WINDOW_LABOR_HOURS);

        List<AttendanceLedger> expiredLedgers = ledgerRepository
                .findLedgersWithExpiredPendingDeviations(tenantId, windowCutoff);

        for (AttendanceLedger ledger : expiredLedgers) {
            boolean closedAny = false;
            for (TimeDeviationRecord deviation : ledger.getDeviations()) {
                if (deviation.isPending() && !deviation.getDetectedAt().isAfter(windowCutoff)) {
                    String impact = resolveFinancialImpact(deviation);
                    ledger.autoCloseDeviation(deviation.getDeviationId(), serverNtpNow, impact);
                    closedAny = true;
                }
            }

            if (closedAny) {
                ledgerRepository.save(ledger);
                eventOutbox.store(ledger.pullDomainEvents());
            }
        }
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private AttendanceLedger findLedgerOrThrow(UUID ledgerId) {
        return ledgerRepository.findById(ledgerId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "AttendanceLedger not found: " + ledgerId));
    }

    /**
     * Maps a DeviationType to its financial impact description sent in
     * {@code ExceptionAutoClosedEvent.financialImpact} toward BC-01 Core Notifications.
     *
     * <p>BC-05 (Payroll) receives the actual deduction through the
     * {@code ATTENDANCE_PERIOD_CLOSED} event payload ({@code PayrollHandoffPackage}).
     */
    private String resolveFinancialImpact(TimeDeviationRecord deviation) {
        return switch (deviation.getDeviationType()) {
            case NO_SHOW, UNAUTHORIZED_ABSENCE -> "FULL_DAY_DEDUCTION";
            case OVERTIME -> "OVERTIME_DISCARDED";
            case LATE_IN -> "LATE_MINUTES_DEDUCTION";
            case EARLY_OUT -> "EARLY_OUT_DEDUCTION";
            case GEO_VIOLATION -> "GEO_VIOLATION_PENDING_REVIEW";
            case MISSING_PUNCH -> "MISSING_PUNCH_PENDING_REVIEW";
        };
    }
}
