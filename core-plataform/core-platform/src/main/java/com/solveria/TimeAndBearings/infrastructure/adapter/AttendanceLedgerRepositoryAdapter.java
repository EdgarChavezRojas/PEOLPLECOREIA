package com.solveria.TimeAndBearings.infrastructure.adapter;

import com.solveria.TimeAndBearings.application.port.outbound.AttendanceLedgerRepositoryPort;
import com.solveria.TimeAndBearings.domain.model.ar.AttendanceLedger;
import com.solveria.TimeAndBearings.domain.model.entity.TimeDeviationRecord;
import com.solveria.TimeAndBearings.domain.model.entity.TimeEntry;
import com.solveria.TimeAndBearings.infrastructure.jpa.AttendanceLedgerJpa;
import com.solveria.TimeAndBearings.infrastructure.jpa.TimeDeviationRecordJpa;
import com.solveria.TimeAndBearings.infrastructure.jpa.TimeEntryTimeAndBearingsJpa;
import com.solveria.TimeAndBearings.infrastructure.mapper.AttendanceLedgerMapper;
import com.solveria.TimeAndBearings.infrastructure.repository.AttendanceLedgerSpringRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Repository Adapter: implements {@link AttendanceLedgerRepositoryPort} using Spring Data JPA and
 * MapStruct.
 *
 * <p>Responsibilities:
 *
 * <ul>
 *   <li>Translate between the domain model ({@link AttendanceLedger}) and the JPA persistence model
 *       ({@link AttendanceLedgerJpa}).
 *   <li>Reconstitute the full aggregate graph (Ledger + TimeEntries + Deviations) when loading from
 *       the database.
 *   <li>Persist the aggregate graph on save; JPA {@code CascadeType.ALL} propagates to child
 *       entities ({@link TimeEntryTimeAndBearingsJpa}, {@link TimeDeviationRecordJpa}).
 * </ul>
 *
 * <p>This class contains NO domain logic. Invariants are enforced exclusively by {@link
 * AttendanceLedger}.
 */
@Component
public class AttendanceLedgerRepositoryAdapter implements AttendanceLedgerRepositoryPort {

  private final AttendanceLedgerSpringRepository springRepository;
  private final AttendanceLedgerMapper mapper;

  public AttendanceLedgerRepositoryAdapter(
      AttendanceLedgerSpringRepository springRepository, AttendanceLedgerMapper mapper) {
    this.springRepository = springRepository;
    this.mapper = mapper;
  }

  // ─── Port implementation ─────────────────────────────────────────────────

  @Override
  public Optional<AttendanceLedger> findByRelationshipAndDate(
      UUID tenantId, UUID relationshipId, LocalDate workDate) {

    return springRepository
        .findByRelationshipAndDate(tenantId, relationshipId, workDate)
        .map(this::reconstitute);
  }

  @Override
  public Optional<AttendanceLedger> findById(UUID ledgerId) {
    return springRepository.findByLedgerId(ledgerId).map(this::reconstitute);
  }

  @Override
  public AttendanceLedger save(AttendanceLedger domain) {
    AttendanceLedgerJpa jpa = toJpaFull(domain);
    AttendanceLedgerJpa saved = springRepository.save(jpa);
    return reconstitute(saved);
  }

  // ─── JPA → Domain reconstitution ─────────────────────────────────────────

  /**
   * Fully reconstitutes the AttendanceLedger aggregate from the JPA graph. Children ({@link
   * TimeEntryTimeAndBearingsJpa}, {@link TimeDeviationRecordJpa}) are fetched lazily by Hibernate
   * within the same transaction; the adapter triggers loading here by iterating the collections.
   */
  private AttendanceLedger reconstitute(AttendanceLedgerJpa jpa) {
    AttendanceLedger domain =
        new AttendanceLedger(
            jpa.getLedgerId(),
            (jpa.getTenantId()),
            jpa.getRelationshipId(),
            jpa.getWorkDate(),
            jpa.getShiftId(),
            jpa.getStatus(),
            jpa.isFinalized(),
            jpa.isRemoteWork(),
            jpa.getRemoteWorkAuthId(),
            jpa.getCreatedAt(),
            jpa.getClosedAt());

    // Reconstitute TimeEntry children
    List<TimeEntry> entries = new ArrayList<>();
    for (TimeEntryTimeAndBearingsJpa entryJpa : jpa.getTimeEntries()) {
      entries.add(reconstituteEntry(entryJpa, jpa));
    }
    domain.loadTimeEntries(entries);

    // Reconstitute TimeDeviationRecord children
    List<TimeDeviationRecord> deviations = new ArrayList<>();
    for (TimeDeviationRecordJpa devJpa : jpa.getDeviations()) {
      deviations.add(mapper.toDomain(devJpa));
    }
    domain.loadDeviations(deviations);

    // Reconstitute WorkedHoursSummary (nullable until CRON runs)
    if (jpa.getWorkedHoursSummary() != null
        && jpa.getWorkedHoursSummary().getRegularHours() != null) {
      domain.setWorkedHoursSummary(mapper.toDomain(jpa.getWorkedHoursSummary()));
    }

    return domain;
  }

  /**
   * Reconstitutes a single {@link TimeEntry} from its JPA model. Uses the mapper's VO helper
   * methods to rebuild {@code PunchContext} and {@code GeoValidationSnapshot} from flat columns.
   */
  private TimeEntry reconstituteEntry(
      TimeEntryTimeAndBearingsJpa jpa, AttendanceLedgerJpa ledgerRef) {
    return new TimeEntry(
        jpa.getEntryId(),
        ledgerRef.getLedgerId(),
        jpa.getPunchTime(),
        jpa.getPunchType(),
        mapper.buildPunchContext(jpa),
        mapper.buildGeoSnapshot(jpa),
        jpa.getDeviceSignature(),
        jpa.isRetroactive(),
        jpa.getRetroactiveApproverId(),
        jpa.getCorrectsEntryId(),
        jpa.isFraudFlag());
  }

  // ─── Domain → JPA ────────────────────────────────────────────────────────

  /**
   * Converts the full domain aggregate graph to the JPA persistence graph. Sets bidirectional
   * parent references on each child before saving so that JPA cascade works correctly.
   */
  private AttendanceLedgerJpa toJpaFull(AttendanceLedger domain) {
    AttendanceLedgerJpa jpa = mapper.toJpa(domain);
    jpa.setTenantId(domain.getTenantId());
    jpa.setLedgerId(domain.getLedgerId());

    // WorkedHoursSummary (nullable)
    if (domain.getWorkedHoursSummary() != null) {
      jpa.setWorkedHoursSummary(mapper.toEmbeddable(domain.getWorkedHoursSummary()));
    }

    // TimeEntry children
    List<TimeEntryTimeAndBearingsJpa> entryJpaList = new ArrayList<>();
    for (TimeEntry entry : domain.getTimeEntries()) {
      TimeEntryTimeAndBearingsJpa entryJpa = mapper.toJpa(entry);
      entryJpa.setLedger(jpa); // set parent reference (JPA bidirectional)
      entryJpaList.add(entryJpa);
    }
    jpa.setTimeEntries(entryJpaList);

    // TimeDeviationRecord children
    List<TimeDeviationRecordJpa> devJpaList = new ArrayList<>();
    for (TimeDeviationRecord dev : domain.getDeviations()) {
      TimeDeviationRecordJpa devJpa = mapper.toJpa(dev);
      devJpa.setLedger(jpa); // set parent reference (JPA bidirectional)
      devJpaList.add(devJpa);
    }
    jpa.setDeviations(devJpaList);

    return jpa;
  }
}
