package com.solveria.TimeAndBearings.infrastructure.jpa;

import com.solveria.TimeAndBearings.domain.model.ar.AttendanceLedger;
import com.solveria.core.shared.base.BaseEntity;
import com.solveria.TimeAndBearings.domain.model.enums.LedgerStatus;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA Entity for {@code attendance_ledger} table.
 * Extends {@link BaseEntity} (multi-tenant filter, audit timestamps, optimistic locking).
 *
 * <p>This class carries NO domain logic. All business behavior lives in
 * {@link AttendanceLedger}.
 */
@Entity
@Table(
        name = "attendance_ledger",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_ledger_relationship_date",
                columnNames = {"relationship_id", "work_date", "tenant_id"}
        )
)
public class AttendanceLedgerJpa extends BaseEntity {


    @Column(name = "ledger_id", nullable = false, updatable = false, columnDefinition = "UUID")
    private UUID ledgerId;

    @Column(name = "relationship_id", nullable = false, updatable = false, columnDefinition = "UUID")
    private UUID relationshipId;

    @Column(name = "work_date", nullable = false, updatable = false)
    private LocalDate workDate;

    @Column(name = "shift_id", columnDefinition = "UUID")
    private UUID shiftId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private LedgerStatus status;

    @Column(name = "is_finalized", nullable = false)
    private boolean finalized;

    @Column(name = "remote_work", nullable = false)
    private boolean remoteWork;

    @Column(name = "remote_work_auth_id", columnDefinition = "UUID")
    private UUID remoteWorkAuthId;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @OneToMany(
            mappedBy = "ledger",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<TimeEntryJpa> timeEntries = new ArrayList<>();

    @OneToMany(
            mappedBy = "ledger",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<TimeDeviationRecordJpa> deviations = new ArrayList<>();

    /** WorkedHoursSummary embedded inline — NULL until CRON calculates it (WF-TM03). */
    @Embedded
    private WorkedHoursSummaryEmbeddable workedHoursSummary;

    public AttendanceLedgerJpa() {}

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public UUID getLedgerId() { return ledgerId; }
    public void setLedgerId(UUID ledgerId) { this.ledgerId = ledgerId; }

    public UUID getRelationshipId() { return relationshipId; }
    public void setRelationshipId(UUID relationshipId) { this.relationshipId = relationshipId; }

    public LocalDate getWorkDate() { return workDate; }
    public void setWorkDate(LocalDate workDate) { this.workDate = workDate; }

    public UUID getShiftId() { return shiftId; }
    public void setShiftId(UUID shiftId) { this.shiftId = shiftId; }

    public LedgerStatus getStatus() { return status; }
    public void setStatus(LedgerStatus status) { this.status = status; }

    public boolean isFinalized() { return finalized; }
    public void setFinalized(boolean finalized) { this.finalized = finalized; }

    public boolean isRemoteWork() { return remoteWork; }
    public void setRemoteWork(boolean remoteWork) { this.remoteWork = remoteWork; }

    public UUID getRemoteWorkAuthId() { return remoteWorkAuthId; }
    public void setRemoteWorkAuthId(UUID remoteWorkAuthId) { this.remoteWorkAuthId = remoteWorkAuthId; }

    public LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }

    public List<TimeEntryJpa> getTimeEntries() { return timeEntries; }
    public void setTimeEntries(List<TimeEntryJpa> timeEntries) { this.timeEntries = timeEntries; }

    public List<TimeDeviationRecordJpa> getDeviations() { return deviations; }
    public void setDeviations(List<TimeDeviationRecordJpa> deviations) { this.deviations = deviations; }

    public WorkedHoursSummaryEmbeddable getWorkedHoursSummary() { return workedHoursSummary; }
    public void setWorkedHoursSummary(WorkedHoursSummaryEmbeddable workedHoursSummary) {
        this.workedHoursSummary = workedHoursSummary;
    }
}
