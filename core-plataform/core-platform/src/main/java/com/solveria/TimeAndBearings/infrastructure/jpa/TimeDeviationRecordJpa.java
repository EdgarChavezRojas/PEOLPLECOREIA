package com.solveria.TimeAndBearings.infrastructure.jpa;

import com.solveria.core.shared.base.BaseEntity;
import com.solveria.TimeAndBearings.domain.model.enums.DeviationType;
import com.solveria.TimeAndBearings.domain.model.enums.ResolutionStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity for {@code time_deviation_record} table.
 *
 * <p>Mutable: the MSS updates {@code resolution_status}, {@code resolved_at},
 * {@code resolved_by}, {@code reason_note}, and {@code secondary_approver_id}
 * during WF-TM02. {@code deviation_type} and {@code detected_at} are immutable.
 */
@Entity
@Table(name = "time_deviation_record")
public class TimeDeviationRecordJpa extends BaseEntity {


    @Column(name = "deviation_id", nullable = false, updatable = false, columnDefinition = "UUID")
    private UUID deviationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ledger_id", nullable = false, updatable = false)
    private AttendanceLedgerJpa ledger;

    @Enumerated(EnumType.STRING)
    @Column(name = "deviation_type", nullable = false, updatable = false, length = 40)
    private DeviationType deviationType;

    @Column(name = "deviation_minutes", nullable = false, updatable = false)
    private int deviationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "resolution_status", nullable = false, length = 40)
    private ResolutionStatus resolutionStatus;

    @Column(name = "detected_at", nullable = false, updatable = false)
    private LocalDateTime detectedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    /** FK to User (MSS/Analyst). NULL if PENDING or AUTO_CLOSED_AS_UNJUSTIFIED. */
    @Column(name = "resolved_by", columnDefinition = "UUID")
    private UUID resolvedBy;

    /** Min 20 chars for APPROVED / OVERRIDDEN_BY_MANAGER (P-TM32). Enforced at domain level. */
    @Column(name = "reason_note", columnDefinition = "TEXT")
    private String reasonNote;

    /** Required for retroactivity >48h (P-TM32). */
    @Column(name = "secondary_approver_id", columnDefinition = "UUID")
    private UUID secondaryApproverId;

    public TimeDeviationRecordJpa() {}

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public UUID getDeviationId() { return deviationId; }
    public void setDeviationId(UUID deviationId) { this.deviationId = deviationId; }

    public AttendanceLedgerJpa getLedger() { return ledger; }
    public void setLedger(AttendanceLedgerJpa ledger) { this.ledger = ledger; }

    public DeviationType getDeviationType() { return deviationType; }
    public void setDeviationType(DeviationType deviationType) { this.deviationType = deviationType; }

    public int getDeviationMinutes() { return deviationMinutes; }
    public void setDeviationMinutes(int deviationMinutes) { this.deviationMinutes = deviationMinutes; }

    public ResolutionStatus getResolutionStatus() { return resolutionStatus; }
    public void setResolutionStatus(ResolutionStatus resolutionStatus) {
        this.resolutionStatus = resolutionStatus;
    }

    public LocalDateTime getDetectedAt() { return detectedAt; }
    public void setDetectedAt(LocalDateTime detectedAt) { this.detectedAt = detectedAt; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    public UUID getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(UUID resolvedBy) { this.resolvedBy = resolvedBy; }

    public String getReasonNote() { return reasonNote; }
    public void setReasonNote(String reasonNote) { this.reasonNote = reasonNote; }

    public UUID getSecondaryApproverId() { return secondaryApproverId; }
    public void setSecondaryApproverId(UUID secondaryApproverId) {
        this.secondaryApproverId = secondaryApproverId;
    }
}
