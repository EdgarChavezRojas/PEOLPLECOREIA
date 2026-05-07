package com.solveria.TimeAndBearings.infrastructure.jpa;

import com.solveria.core.shared.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity: {@code daily_consolidation_summary} — Persistencia de la entidad
 * {@code DailyConsolidationSummary} (hijo del Aggregate 16: TimesheetPeriod).
 *
 * <p>Gestionado exclusivamente por el AR {@code TimesheetPeriod}; sin repositorio
 * propio. La cascada se gestiona desde {@link TimesheetPeriodJpa}.
 */
@Entity
@Table(name = "daily_consolidation_summary")
public class DailyConsolidationSummaryJpa extends BaseEntity {

    @Column(name = "summary_id", nullable = false, unique = true, updatable = false,
            columnDefinition = "UUID")
    private UUID summaryId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "period_id", nullable = false, updatable = false)
    private TimesheetPeriodJpa timesheetPeriod;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "total_scheduled", nullable = false)
    private int totalScheduled;

    @Column(name = "total_attended", nullable = false)
    private int totalAttended;

    @Column(name = "total_no_shows", nullable = false)
    private int totalNoShows;

    @Column(name = "total_exceptions_pending", nullable = false)
    private int totalExceptionsPending;

    @Column(name = "total_regular_hours", nullable = false, precision = 8, scale = 2)
    private BigDecimal totalRegularHours;

    @Column(name = "total_overtime_hours", nullable = false, precision = 8, scale = 2)
    private BigDecimal totalOvertimeHours;

    @Column(name = "total_night_hours", nullable = false, precision = 8, scale = 2)
    private BigDecimal totalNightHours;

    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;

    public DailyConsolidationSummaryJpa() {
        // JPA
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public UUID getSummaryId() { return summaryId; }
    public void setSummaryId(UUID summaryId) { this.summaryId = summaryId; }

    public TimesheetPeriodJpa getTimesheetPeriod() { return timesheetPeriod; }
    public void setTimesheetPeriod(TimesheetPeriodJpa timesheetPeriod) {
        this.timesheetPeriod = timesheetPeriod;
    }

    public LocalDate getWorkDate() { return workDate; }
    public void setWorkDate(LocalDate workDate) { this.workDate = workDate; }

    public int getTotalScheduled() { return totalScheduled; }
    public void setTotalScheduled(int totalScheduled) { this.totalScheduled = totalScheduled; }

    public int getTotalAttended() { return totalAttended; }
    public void setTotalAttended(int totalAttended) { this.totalAttended = totalAttended; }

    public int getTotalNoShows() { return totalNoShows; }
    public void setTotalNoShows(int totalNoShows) { this.totalNoShows = totalNoShows; }

    public int getTotalExceptionsPending() { return totalExceptionsPending; }
    public void setTotalExceptionsPending(int totalExceptionsPending) {
        this.totalExceptionsPending = totalExceptionsPending;
    }

    public BigDecimal getTotalRegularHours() { return totalRegularHours; }
    public void setTotalRegularHours(BigDecimal totalRegularHours) {
        this.totalRegularHours = totalRegularHours;
    }

    public BigDecimal getTotalOvertimeHours() { return totalOvertimeHours; }
    public void setTotalOvertimeHours(BigDecimal totalOvertimeHours) {
        this.totalOvertimeHours = totalOvertimeHours;
    }

    public BigDecimal getTotalNightHours() { return totalNightHours; }
    public void setTotalNightHours(BigDecimal totalNightHours) {
        this.totalNightHours = totalNightHours;
    }

    public LocalDateTime getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(LocalDateTime calculatedAt) { this.calculatedAt = calculatedAt; }
}
