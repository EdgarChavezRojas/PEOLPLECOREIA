package com.solveria.payroll.infrastructure.jpa;

import com.solveria.core.shared.base.BaseEntity;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * JPA Entity: {@code prl_payroll_period} — Persistencia del Periodo de Nómina.
 *
 * <p>Extiende {@link BaseEntity} para herencia de {@code tenant_id}, {@code created_at},
 * {@code created_by}, {@code last_modified_at}, {@code last_modified_by} y {@code version}.
 */
@Entity
@Table(name = "prl_payroll_period")
public class PayrollPeriodJpa extends BaseEntity {

    @Column(name = "period_id", nullable = false, unique = true, updatable = false,
            columnDefinition = "UUID")
    private UUID periodId;

    @Column(name = "month", nullable = false)
    private Integer month;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "cutoff_date", nullable = false)
    private LocalDate cutoffDate;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "holiday_calendar_ref", columnDefinition = "UUID")
    private UUID holidayCalendarRef;

    public PayrollPeriodJpa() {
        // JPA
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public UUID getPeriodId() { return periodId; }
    public void setPeriodId(UUID periodId) { this.periodId = periodId; }

    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public LocalDate getCutoffDate() { return cutoffDate; }
    public void setCutoffDate(LocalDate cutoffDate) { this.cutoffDate = cutoffDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public UUID getHolidayCalendarRef() { return holidayCalendarRef; }
    public void setHolidayCalendarRef(UUID holidayCalendarRef) { this.holidayCalendarRef = holidayCalendarRef; }
}
