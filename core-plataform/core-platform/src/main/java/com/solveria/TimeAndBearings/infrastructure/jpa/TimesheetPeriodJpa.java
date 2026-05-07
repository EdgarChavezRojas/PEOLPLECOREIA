package com.solveria.TimeAndBearings.infrastructure.jpa;

import com.solveria.core.shared.base.BaseEntity;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA Entity: {@code timesheet_period} — Persistencia del Aggregate Root 16.
 *
 * <p>Extiende {@link BaseEntity} para herencia de {@code tenant_id}, {@code created_at},
 * {@code created_by}, {@code last_modified_at}, {@code last_modified_by} y {@code version}.
 *
 * <p>La llave primaria de negocio ({@code period_id}) es un UUID separado del
 * {@code Long id} de {@link BaseEntity}, siguiendo el patrón establecido en el módulo.
 */
@Entity
@Table(name = "timesheet_period")
public class TimesheetPeriodJpa extends BaseEntity {

    @Column(name = "period_id", nullable = false, unique = true, updatable = false,
            columnDefinition = "UUID")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID periodId;

    @Column(name = "org_unit_id", nullable = false, updatable = false, columnDefinition = "UUID")
    private UUID orgUnitId;

    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", nullable = false, length = 20)
    private String periodType;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(name = "grace_period_end", nullable = false)
    private LocalDateTime gracePeriodEnd;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "closed_by", columnDefinition = "UUID")
    private UUID closedBy;

    @Column(name = "closure_type", length = 10)
    private String closureType;

    @Column(name = "payroll_event_emitted_at")
    private LocalDateTime payrollEventEmittedAt;

    @OneToMany(
            mappedBy = "timesheetPeriod",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<DailyConsolidationSummaryJpa> dailySummaries = new ArrayList<>();

    @OneToOne(
            mappedBy = "timesheetPeriod",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private PayrollHandoffPackageJpa handoffPackage;

    public TimesheetPeriodJpa() {
        // JPA
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public UUID getPeriodId() { return periodId; }
    public void setPeriodId(UUID periodId) { this.periodId = periodId; }

    public UUID getOrgUnitId() { return orgUnitId; }
    public void setOrgUnitId(UUID orgUnitId) { this.orgUnitId = orgUnitId; }

    public String getPeriodType() { return periodType; }
    public void setPeriodType(String periodType) { this.periodType = periodType; }

    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }

    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }

    public LocalDateTime getGracePeriodEnd() { return gracePeriodEnd; }
    public void setGracePeriodEnd(LocalDateTime gracePeriodEnd) { this.gracePeriodEnd = gracePeriodEnd; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }

    public UUID getClosedBy() { return closedBy; }
    public void setClosedBy(UUID closedBy) { this.closedBy = closedBy; }

    public String getClosureType() { return closureType; }
    public void setClosureType(String closureType) { this.closureType = closureType; }

    public LocalDateTime getPayrollEventEmittedAt() { return payrollEventEmittedAt; }
    public void setPayrollEventEmittedAt(LocalDateTime payrollEventEmittedAt) {
        this.payrollEventEmittedAt = payrollEventEmittedAt;
    }

    public List<DailyConsolidationSummaryJpa> getDailySummaries() { return dailySummaries; }
    public void setDailySummaries(List<DailyConsolidationSummaryJpa> dailySummaries) {
        this.dailySummaries = dailySummaries;
    }

    public PayrollHandoffPackageJpa getHandoffPackage() { return handoffPackage; }
    public void setHandoffPackage(PayrollHandoffPackageJpa handoffPackage) {
        this.handoffPackage = handoffPackage;
    }
}
