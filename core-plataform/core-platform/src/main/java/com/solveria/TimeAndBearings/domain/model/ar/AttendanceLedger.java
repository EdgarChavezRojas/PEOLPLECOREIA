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
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root 14: AttendanceLedger (Libro Mayor de Asistencia).
 *
 * <p>Contenedor diario de todos los eventos de marcación de un colaborador. Fuente de verdad para
 * el cálculo de nómina. Puro Java 21 — sin anotaciones Spring/JPA.
 *
 * <h3>Invariantes Enforced</h3>
 *
 * <ol>
 *   <li><b>Active Punch Uniqueness:</b> Solo un PUNCH_IN activo sin PUNCH_OUT por ledger.
 *   <li><b>Chronological Integrity:</b> punch_time ≤ serverNtpNow + 5s; PUNCH_OUT &gt; PUNCH_IN.
 *   <li><b>Attendance Closure Parity:</b> No CLOSED con PUNCHes impares sin OVERRIDDEN_BY_MANAGER
 *       MISSING_PUNCH.
 *   <li><b>Finalized Record Immutability:</b> is_finalized=TRUE → ningún campo hijo es modificable
 *       (P-TM33).
 * </ol>
 *
 * <h3>Non-Blocking Design (WF-TM01)</h3>
 *
 * Geo o Auth failures NO bloquean el dispositivo. El TimeEntry se persiste con {@code
 * geo_status=OUTSIDE_FENCE} y un {@code TimeDeviationRecord} se crea asincrónicamente. El evento
 * {@code PunchAnomalyDetectedEvent} se publica al Message Broker.
 */
public class AttendanceLedger {

  private static final int NTP_TOLERANCE_SECONDS = 5;

  private UUID ledgerId;
  private UUID tenantId;
  private UUID relationshipId;
  private LocalDate workDate;
  private UUID shiftId;
  // orgUnitId se persiste de forma denormalizada para soportar consultas de consolidacion/cierre
  // (P-TM33, CRON) directamente sobre AttendanceLedger, evitando joins con BC-01 y manteniendo
  // el aislamiento entre bounded contexts. Esto permite filtrar por unidad organizativa en rangos
  // de fechas sin depender de tablas externas; por eso no es un dato operativo del flujo de
  // marcacion, sino un dato de particion para lectura y reportes.
  private UUID orgUnitId;
  private LedgerStatus status;
  private boolean finalized;
  private boolean remoteWork;
  private UUID remoteWorkAuthId;
  private Instant createdAt;
  private LocalDateTime closedAt;

  private List<TimeEntry> timeEntries = new ArrayList<>();
  private List<TimeDeviationRecord> deviations = new ArrayList<>();
  private WorkedHoursSummary workedHoursSummary;

  private List<DomainEvent> domainEvents = new ArrayList<>();

  public static AttendanceLedger open(
      UUID tenantId,
      UUID relationshipId,
      UUID orgUnitId,
      LocalDate workDate,
      UUID shiftId,
      Instant serverNtpNow) {

    return new AttendanceLedger(
        UUID.randomUUID(),
        tenantId,
        relationshipId,
        orgUnitId,
        workDate,
        shiftId,
        LedgerStatus.OPEN,
        false,
        false,
        null,
        serverNtpNow,
        null);
  }

  public AttendanceLedger(
      UUID ledgerId,
      UUID tenantId,
      UUID relationshipId,
      UUID orgUnitId,
      LocalDate workDate,
      UUID shiftId,
      LedgerStatus status,
      boolean finalized,
      boolean remoteWork,
      UUID remoteWorkAuthId,
      Instant createdAt,
      LocalDateTime closedAt) {

    this.ledgerId = ledgerId;
    this.tenantId = tenantId;
    this.relationshipId = relationshipId;
    this.orgUnitId = Objects.requireNonNull(orgUnitId, "orgUnitId es requerido");
    this.workDate = workDate;
    this.shiftId = shiftId;
    this.status = status;
    this.finalized = finalized;
    this.remoteWork = remoteWork;
    this.remoteWorkAuthId = remoteWorkAuthId;
    this.createdAt = createdAt;
    this.closedAt = closedAt;
  }

  public TimeEntry recordPunch(
      LocalDateTime serverNtpNow,
      PunchType punchType,
      PunchContext punchContext,
      GeoValidationSnapshot geoSnapshot,
      String deviceSignature,
      boolean fraudFlag) {

    assertNotFinalized("recordPunch");
    assertNtpCompliance(serverNtpNow);

    if (punchType == PunchType.PUNCH_IN) {
      long activePunchIns =
          timeEntries.stream()
              .filter(e -> e.getPunchType() == PunchType.PUNCH_IN)
              .filter(
                  e ->
                      timeEntries.stream()
                          .noneMatch(
                              out ->
                                  out.getPunchType() == PunchType.PUNCH_OUT
                                      && out.getPunchTime().isAfter(e.getPunchTime())))
              .count();
      if (activePunchIns > 0) {
        throw new ActivePunchAlreadyExistsException(ledgerId, relationshipId);
      }
    }

    if (punchType == PunchType.PUNCH_OUT || punchType == PunchType.MEAL_START) {
      timeEntries.stream()
          .filter(e -> e.getPunchType() == PunchType.PUNCH_IN)
          .max((a, b) -> a.getPunchTime().compareTo(b.getPunchTime()))
          .ifPresent(
              lastIn -> {
                if (!serverNtpNow.isAfter(lastIn.getPunchTime())) {
                  throw new ChronologicalIntegrityException(
                      ledgerId,
                      serverNtpNow,
                      "Rule A: PUNCH_OUT/MEAL_START must be strictly after the last PUNCH_IN at "
                          + lastIn.getPunchTime());
                }
              });
    }

    TimeEntry entry =
        new TimeEntry(
            UUID.randomUUID(),
            ledgerId,
            serverNtpNow,
            punchType,
            punchContext,
            geoSnapshot,
            deviceSignature,
            false,
            null,
            null,
            fraudFlag);

    timeEntries.add(entry);

    if (geoSnapshot != null && geoSnapshot.geoStatus() == GeoStatus.OUTSIDE_FENCE) {
      this.status = LedgerStatus.PENDING_REVIEW;
      domainEvents.add(
          new PunchAnomalyDetectedEvent(
              ledgerId,
              relationshipId,
              DeviationType.GEO_VIOLATION,
              serverNtpNow.toInstant(ZoneOffset.UTC),
              tenantId));
    }

    return entry;
  }

  public TimeEntry recordRetroactivePunch(
      LocalDateTime serverNtpNow,
      LocalDateTime retroactivePunchTime,
      PunchType punchType,
      UUID mssActorId,
      UUID secondaryApproverId,
      PunchContext punchContext,
      UUID correctsEntryId) {

    if (this.status == LedgerStatus.CLOSED || this.finalized) {
      throw new PeriodLockedException(ledgerId);
    }
    assertNotFinalized("recordRetroactivePunch");
    assertNtpCompliance(serverNtpNow);

    if (retroactivePunchTime.isAfter(serverNtpNow.plusSeconds(NTP_TOLERANCE_SECONDS))) {
      throw new ChronologicalIntegrityException(
          ledgerId,
          retroactivePunchTime,
          "Rule B: retroactivePunchTime cannot be in the future relative to server NTP.");
    }

    TimeEntry entry =
        new TimeEntry(
            UUID.randomUUID(),
            ledgerId,
            retroactivePunchTime,
            punchType,
            punchContext,
            GeoValidationSnapshot.noGps(),
            null,
            true,
            mssActorId,
            correctsEntryId,
            false);

    timeEntries.add(entry);
    return entry;
  }

  public TimeDeviationRecord registerDeviation(
      DeviationType deviationType, int deviationMinutes, LocalDateTime detectedAt) {

    assertNotFinalized("registerDeviation");

    TimeDeviationRecord record =
        new TimeDeviationRecord(
            UUID.randomUUID(), ledgerId, deviationType, deviationMinutes, detectedAt);
    deviations.add(record);

    if (this.status == LedgerStatus.OPEN) {
      this.status = LedgerStatus.PENDING_REVIEW;
    }

    if (deviationType == DeviationType.LATE_IN) {
      domainEvents.add(
          new PunchAnomalyDetectedEvent(
              ledgerId,
              relationshipId,
              DeviationType.LATE_IN,
              detectedAt.toInstant(ZoneOffset.UTC),
              tenantId));
    }

    return record;
  }

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

    domainEvents.add(
        TimeDeviationJustifiedEvent.of(
            ledgerId,
            deviationId,
            actorId,
            newStatus,
            resolvedAt.toInstant(ZoneOffset.UTC),
            tenantId));

    boolean anyPending = deviations.stream().anyMatch(TimeDeviationRecord::isPending);
    if (!anyPending && this.status == LedgerStatus.PENDING_REVIEW) {
      this.status = LedgerStatus.OPEN;
    }
  }

  public void autoCloseDeviation(UUID deviationId, LocalDateTime closedAt, String financialImpact) {
    TimeDeviationRecord record = findDeviationOrThrow(deviationId);
    record.autoClose(closedAt);

    domainEvents.add(
        new ExceptionAutoClosedEvent(
            deviationId,
            relationshipId,
            ResolutionStatus.AUTO_CLOSED_AS_UNJUSTIFIED,
            "WINDOW_EXPIRED",
            financialImpact,
            closedAt.toInstant(ZoneOffset.UTC),
            tenantId));
  }

  public void setWorkedHoursSummary(WorkedHoursSummary summary) {
    assertNotFinalized("setWorkedHoursSummary");
    if (summary == null) throw new IllegalArgumentException("WorkedHoursSummary cannot be null.");
    this.workedHoursSummary = summary;
  }

  public void close(LocalDateTime closedAt) {
    assertNotFinalized("close");

    if (this.workedHoursSummary == null) {
      throw new IllegalStateException(
          "AttendanceLedger [" + ledgerId + "] cannot be closed without a WorkedHoursSummary.");
    }

    long uncoveredOpenPunches = countUncoveredOpenPunches();
    if (uncoveredOpenPunches > 0) {
      throw new AttendanceClosureParityException(ledgerId, (int) uncoveredOpenPunches);
    }

    long pendingDeviations = deviations.stream().filter(TimeDeviationRecord::isPending).count();
    if (pendingDeviations > 0) {
      throw new IllegalStateException(
          "AttendanceLedger ["
              + ledgerId
              + "] has "
              + pendingDeviations
              + " PENDING deviation(s). Resolve them before closing (WF-TM02).");
    }

    this.status = LedgerStatus.CLOSED;
    this.finalized = true;
    this.closedAt = closedAt;
  }

  public void authorizeRemoteWork(UUID remoteWorkAuthId) {
    assertNotFinalized("authorizeRemoteWork");
    this.remoteWork = true;
    this.remoteWorkAuthId = remoteWorkAuthId;
  }

  public List<DomainEvent> pullDomainEvents() {
    List<DomainEvent> events = new ArrayList<>(domainEvents);
    domainEvents.clear();
    return Collections.unmodifiableList(events);
  }

  private void assertNotFinalized(String operation) {
    if (this.finalized) {
      throw new ClosedRecordMutationException(ledgerId, operation);
    }
  }

  private void assertNtpCompliance(LocalDateTime serverNtpNow) {
    if (serverNtpNow.isAfter(
        LocalDateTime.now(ZoneOffset.UTC).plusSeconds(NTP_TOLERANCE_SECONDS))) {
      throw new ChronologicalIntegrityException(
          ledgerId,
          serverNtpNow,
          "Rule B: punch_time is in the future beyond the NTP tolerance of "
              + NTP_TOLERANCE_SECONDS
              + " seconds (P-TM26).");
    }
  }

  private long countUncoveredOpenPunches() {
    long openPunchIns =
        timeEntries.stream()
            .filter(e -> e.getPunchType() == PunchType.PUNCH_IN)
            .filter(
                e ->
                    timeEntries.stream()
                        .noneMatch(
                            out ->
                                out.getPunchType() == PunchType.PUNCH_OUT
                                    && out.getPunchTime().isAfter(e.getPunchTime())))
            .count();

    long coveredByOverride =
        deviations.stream().filter(TimeDeviationRecord::coversOpenPunchGap).count();

    return Math.max(0, openPunchIns - coveredByOverride);
  }

  private TimeDeviationRecord findDeviationOrThrow(UUID deviationId) {
    return deviations.stream()
        .filter(d -> d.getDeviationId().equals(deviationId))
        .findFirst()
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "TimeDeviationRecord ["
                        + deviationId
                        + "] not found in ledger ["
                        + ledgerId
                        + "]."));
  }

  public UUID getLedgerId() {
    return ledgerId;
  }

  public UUID getTenantId() {
    return tenantId;
  }

  public UUID getRelationshipId() {
    return relationshipId;
  }

  public LocalDate getWorkDate() {
    return workDate;
  }

  public UUID getShiftId() {
    return shiftId;
  }

  public LedgerStatus getStatus() {
    return status;
  }

  public boolean isFinalized() {
    return finalized;
  }

  public boolean isRemoteWork() {
    return remoteWork;
  }

  public UUID getRemoteWorkAuthId() {
    return remoteWorkAuthId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getClosedAt() {
    return closedAt;
  }

  public List<TimeEntry> getTimeEntries() {
    return Collections.unmodifiableList(timeEntries);
  }

  public List<TimeDeviationRecord> getDeviations() {
    return Collections.unmodifiableList(deviations);
  }

  public WorkedHoursSummary getWorkedHoursSummary() {
    return workedHoursSummary;
  }

  public void setLedgerId(UUID ledgerId) {
    this.ledgerId = ledgerId;
  }

  public void setTenantId(UUID tenantId) {
    this.tenantId = tenantId;
  }

  public void setRelationshipId(UUID relationshipId) {
    this.relationshipId = relationshipId;
  }

  public void setWorkDate(LocalDate workDate) {
    this.workDate = workDate;
  }

  public void setShiftId(UUID shiftId) {
    this.shiftId = shiftId;
  }

  public void setStatus(LedgerStatus status) {
    this.status = status;
  }

  public void setFinalized(boolean finalized) {
    this.finalized = finalized;
  }

  public void setRemoteWork(boolean remoteWork) {
    this.remoteWork = remoteWork;
  }

  public void setRemoteWorkAuthId(UUID remoteWorkAuthId) {
    this.remoteWorkAuthId = remoteWorkAuthId;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public void setClosedAt(LocalDateTime closedAt) {
    this.closedAt = closedAt;
  }

  public void setTimeEntries(List<TimeEntry> timeEntries) {
    this.timeEntries = timeEntries;
  }

  public void setDeviations(List<TimeDeviationRecord> deviations) {
    this.deviations = deviations;
  }

  public void loadTimeEntries(List<TimeEntry> entries) {
    this.timeEntries.addAll(entries);
  }

  public void loadDeviations(List<TimeDeviationRecord> devs) {
    this.deviations.addAll(devs);
  }

  public UUID getOrgUnitId() {
    return orgUnitId;
  }

  public void setOrgUnitId(UUID orgUnitId) {
    this.orgUnitId = orgUnitId;
  }
}
