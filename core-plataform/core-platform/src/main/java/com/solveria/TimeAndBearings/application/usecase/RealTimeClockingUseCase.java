package com.solveria.TimeAndBearings.application.usecase;

import com.solveria.TimeAndBearings.application.command.ClockCommand;
import com.solveria.TimeAndBearings.application.port.inbound.RealTimeClockingPort;
import com.solveria.TimeAndBearings.application.port.outbound.AttendanceLedgerRepositoryPort;
import com.solveria.TimeAndBearings.application.port.outbound.EventOutboxPort;
import com.solveria.TimeAndBearings.domain.model.ar.AttendanceLedger;
import com.solveria.TimeAndBearings.domain.model.entity.TimeEntry;
import com.solveria.TimeAndBearings.domain.model.enums.DeviationType;
import com.solveria.TimeAndBearings.domain.model.enums.GeoStatus;
import com.solveria.TimeAndBearings.domain.model.enums.PunchSource;
import com.solveria.TimeAndBearings.domain.model.enums.PunchType;
import com.solveria.TimeAndBearings.domain.model.vo.GeoValidationSnapshot;
import com.solveria.TimeAndBearings.domain.model.vo.PunchContext;
import com.solveria.core.shared.events.DomainEvent;
import java.time.*;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Use Case: Real-Time Clocking (WF-TM01).
 *
 * <p>Orchestrates steps 2–8 of WF-TM01:
 *
 * <ol>
 *   <li>Assigns server NTP time — client timestamp is NEVER used (P-TM26).
 *   <li>Idempotency check: P-TM27 (5-minute anti-double-punch window).
 *   <li>Geo-extension validation: P-TM28 (Extension-Based Geo-Fencing, GEO-04).
 *   <li>Punch type resolution (step 5 WF-TM01).
 *   <li>Delegates invariant enforcement to the AR ({@link AttendanceLedger#recordPunch}).
 *   <li>Async deviation registration (LATE_IN, GEO_VIOLATION) — Non-Blocking Design.
 *   <li>Domain events stored in outbox within the same transaction.
 * </ol>
 *
 * <p><b>Non-Blocking Design (WF-TM01):</b> Geo or auth failures MUST NOT block the device. {@link
 * AttendanceLedger#recordPunch} persists the TimeEntry even with {@code geo_status=OUTSIDE_FENCE}.
 * The deviation is then registered asynchronously via {@link AttendanceLedger#registerDeviation}.
 */
public class RealTimeClockingUseCase implements RealTimeClockingPort {

  /** Anti-double-punch idempotency window in minutes (P-TM27). Configurable per tenant. */
  private static final int IDEMPOTENCY_WINDOW_MINUTES = 5;

  /** Shift late-in threshold in minutes (P-TM26). Configurable per tenant. */
  private static final int LATE_IN_THRESHOLD_MINUTES = 5;

  private final AttendanceLedgerRepositoryPort ledgerRepository;
  private final EventOutboxPort eventOutbox;

  public RealTimeClockingUseCase(
      AttendanceLedgerRepositoryPort ledgerRepository, EventOutboxPort eventOutbox) {
    this.ledgerRepository = ledgerRepository;
    this.eventOutbox = eventOutbox;
  }

  @Override
  public TimeEntry clock(ClockCommand cmd) {

    // ── Step 2: Server NTP Time — never trust the client (P-TM26) ──────────
    Instant serverNtpNow = Instant.now(Clock.system(ZoneOffset.UTC));

    // ── Step 5: Resolve or create the AttendanceLedger for today ──────────
    LocalDate workDate = serverNtpNow.atZone(ZoneOffset.UTC).toLocalDate();
    UUID orgUnitId = Objects.requireNonNull(cmd.orgUnitId(), "orgUnitId es requerido");
    AttendanceLedger ledger =
        ledgerRepository
            .findByRelationshipAndDate(cmd.tenantId(), cmd.relationshipId(), workDate)
            .orElseGet(
                () ->
                    AttendanceLedger.open(
                        cmd.tenantId(),
                        cmd.relationshipId(),
                        orgUnitId,
                        workDate,
                        null,
                        serverNtpNow));

    // ── Step 3 (P-TM27): Anti-Double-Punch Idempotency ───────────────────
    PunchType resolvedPunchType = resolvePunchType(ledger);
    TimeEntry existingIdempotent =
        findIdempotentEntry(ledger, resolvedPunchType, LocalDateTime.from(serverNtpNow));
    if (existingIdempotent != null) {
      // Return original entry — idempotent response, device stays operational
      return existingIdempotent;
    }

    // ── Step 4 (P-TM28): Extension-Based Geo Validation ──────────────────
    GeoValidationSnapshot geoSnapshot = buildGeoSnapshot(cmd);

    // ── Step 6: Build PunchContext ─────────────────────────────────────────
    PunchContext punchContext =
        new PunchContext(cmd.deviceId(), cmd.source(), cmd.ipAddress(), cmd.userAgent());

    // ── Delegate to AR (enforces all 4 invariants) ───────────────────────
    TimeEntry entry =
        ledger.recordPunch(
            LocalDateTime.from(serverNtpNow),
            resolvedPunchType,
            punchContext,
            geoSnapshot,
            cmd.deviceSignature(),
            cmd.fraudFlag());

    // ── Step 7 (P-TM26/P-TM27): Async Deviation Registration (Non-Blocking) ──
    registerDeviationsAsync(ledger, entry, serverNtpNow);

    // ── Persist aggregate + outbox in same transaction ────────────────────
    ledgerRepository.save(ledger);
    List<DomainEvent> events = ledger.pullDomainEvents();
    eventOutbox.store(events);

    return entry;
  }

  // ── Private Orchestration Helpers ─────────────────────────────────────────

  /**
   * Determines the PunchType based on the current state of the Ledger (WF-TM01 step 5). PUNCH_IN if
   * no active entry without PUNCH_OUT; PUNCH_OUT otherwise.
   */
  private PunchType resolvePunchType(AttendanceLedger ledger) {
    boolean hasActivePunchIn =
        ledger.getTimeEntries().stream()
            .filter(e -> e.getPunchType() == PunchType.PUNCH_IN)
            .anyMatch(
                punchIn ->
                    ledger.getTimeEntries().stream()
                        .noneMatch(
                            out ->
                                out.getPunchType() == PunchType.PUNCH_OUT
                                    && out.getPunchTime().isAfter(punchIn.getPunchTime())));

    if (!hasActivePunchIn) {
      return PunchType.PUNCH_IN;
    }
    // Future: detect MEAL_START / MEAL_END based on shift configuration
    return PunchType.PUNCH_OUT;
  }

  /**
   * P-TM27: If same relationship + same punchType was recorded within the 5-min window, return the
   * original TimeEntry (idempotent). Device stays operational regardless.
   */
  private TimeEntry findIdempotentEntry(
      AttendanceLedger ledger, PunchType punchType, LocalDateTime serverNtpNow) {
    LocalDateTime windowStart = serverNtpNow.minusMinutes(IDEMPOTENCY_WINDOW_MINUTES);
    return ledger.getTimeEntries().stream()
        .filter(e -> e.getPunchType() == punchType)
        .filter(
            e -> e.getPunchTime().isAfter(windowStart) && !e.getPunchTime().isAfter(serverNtpNow))
        .findFirst()
        .orElse(null);
  }

  /**
   * P-TM28 [GEO-04]: Build GeoValidationSnapshot based on channel and remote work mode.
   *
   * <ul>
   *   <li>KIOSK / WEB / BIOMETRIC_READER / MANUAL → NO_GPS.
   *   <li>MOBILE + remoteWorkAuthId present → REMOTE_AUTHORIZED (no zone restriction).
   *   <li>MOBILE + no auth → validate department extension via org_extension.
   * </ul>
   */
  private GeoValidationSnapshot buildGeoSnapshot(ClockCommand cmd) {
    if (cmd.source() == PunchSource.KIOSK
        || cmd.source() == PunchSource.BIOMETRIC_READER
        || cmd.source() == PunchSource.WEB
        || cmd.source() == PunchSource.MANUAL) {
      return GeoValidationSnapshot.noGps();
    }

    // Mobile channel — no GPS provided
    if (cmd.latitude() == null || cmd.longitude() == null) {
      return GeoValidationSnapshot.noGps();
    }

    // Remote work authorized (COMISION_SERVICIO / CONTINGENCIA) — no zone restriction (P-TM28)
    if (cmd.remoteWorkAuthId() != null) {
      return GeoValidationSnapshot.remoteAuthorized(
          cmd.latitude(), cmd.longitude(), cmd.accuracyMeters(), cmd.orgExtension());
    }

    // Standard geo-zone validation (P-TM28):
    // The ACL resolves GPS coords → department and passes org_extension in the command.
    // is_within_extension = TRUE when the device's GPS corresponds to the org_extension dept.
    // In production, a dedicated GeoResolutionService (ACL) computes this boolean.
    boolean isWithin = cmd.orgExtension() != null; // ACL pre-resolves; null means no match
    GeoStatus geoStatus = isWithin ? GeoStatus.INSIDE : GeoStatus.OUTSIDE_FENCE;

    return new GeoValidationSnapshot(
        cmd.latitude(),
        cmd.longitude(),
        cmd.accuracyMeters(),
        cmd.orgExtension(),
        isWithin,
        geoStatus);
  }

  /**
   * Registers deviations asynchronously AFTER the TimeEntry is persisted. Non-Blocking Design:
   * these records are created after the main flow completes. The GEO_VIOLATION deviation is only
   * registered if the snapshot reports OUTSIDE_FENCE. The LATE_IN deviation compares punch_time
   * against the AssignedShift.expected_start (resolved by the ACL from BC-SCH — simplified here;
   * full impl wires ShiftReference).
   */
  private void registerDeviationsAsync(
      AttendanceLedger ledger, TimeEntry entry, Instant serverNtpNow) {
    // GEO_VIOLATION deviation (Non-Blocking, P-TM28)
    if (entry.getGeoSnapshot() != null
        && entry.getGeoSnapshot().geoStatus() == GeoStatus.OUTSIDE_FENCE) {
      ledger.registerDeviation(DeviationType.GEO_VIOLATION, 0, LocalDateTime.from(serverNtpNow));
    }

    // LATE_IN deviation: only for PUNCH_IN on a scheduled shift (P-TM26/WF-TM01 step 7).
    // Deviation minutes are calculated by comparing against AssignedShift.expected_start.
    // Placeholder: if punch_time minute-of-hour exceeds threshold, register LATE_IN.
    if (entry.getPunchType() == PunchType.PUNCH_IN && ledger.getShiftId() != null) {
      int minutesLate = entry.getPunchTime().getMinute(); // real impl: vs expected_start
      if (minutesLate > LATE_IN_THRESHOLD_MINUTES) {
        ledger.registerDeviation(
            DeviationType.LATE_IN, minutesLate, LocalDateTime.from(serverNtpNow));
      }
    }
  }
}
