package com.solveria.TimeAndBearings.infrastructure.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA {@code @Embeddable} component for the {@code WorkedHoursSummary} Value Object.
 *
 * <p>Stored inline in the {@code attendance_ledger} table — no join table required.
 * All fields are NULL until the CRON consolidation job (WF-TM03) calculates the summary.
 * Once the ledger transitions to CLOSED ({@code is_finalized=TRUE}), these values are
 * immutable at the domain level (P-TM33 / Finalized Record Immutability invariant).
 */
@Embeddable
public class WorkedHoursSummaryEmbeddable {

    @Column(name = "regular_hours", precision = 6, scale = 2)
    private BigDecimal regularHours;

    @Column(name = "overtime_hours", precision = 6, scale = 2)
    private BigDecimal overtimeHours;

    /** Hours between 22:00–06:00 (recargo 25% LGT Bolivia). */
    @Column(name = "night_hours", precision = 6, scale = 2)
    private BigDecimal nightHours;

    /** Hours on official holiday (recargo 100% LGT Bolivia). */
    @Column(name = "holiday_hours", precision = 6, scale = 2)
    private BigDecimal holidayHours;

    @Column(name = "deducted_break_minutes")
    private Integer deductedBreakMinutes;

    /** Calculated: regular + overtime + night + holiday – (deducted/60). */
    @Column(name = "net_payable_hours", precision = 6, scale = 2)
    private BigDecimal netPayableHours;

    @Column(name = "summary_calculated_at")
    private LocalDateTime calculatedAt;

    public WorkedHoursSummaryEmbeddable() {}

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public BigDecimal getRegularHours() { return regularHours; }
    public void setRegularHours(BigDecimal regularHours) { this.regularHours = regularHours; }

    public BigDecimal getOvertimeHours() { return overtimeHours; }
    public void setOvertimeHours(BigDecimal overtimeHours) { this.overtimeHours = overtimeHours; }

    public BigDecimal getNightHours() { return nightHours; }
    public void setNightHours(BigDecimal nightHours) { this.nightHours = nightHours; }

    public BigDecimal getHolidayHours() { return holidayHours; }
    public void setHolidayHours(BigDecimal holidayHours) { this.holidayHours = holidayHours; }

    public Integer getDeductedBreakMinutes() { return deductedBreakMinutes; }
    public void setDeductedBreakMinutes(Integer deductedBreakMinutes) {
        this.deductedBreakMinutes = deductedBreakMinutes;
    }

    public BigDecimal getNetPayableHours() { return netPayableHours; }
    public void setNetPayableHours(BigDecimal netPayableHours) {
        this.netPayableHours = netPayableHours;
    }

    public LocalDateTime getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(LocalDateTime calculatedAt) { this.calculatedAt = calculatedAt; }
}
