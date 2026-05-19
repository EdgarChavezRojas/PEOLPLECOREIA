package com.solveria.payroll.domain.model.entity;

import com.solveria.payroll.domain.model.vo.CutoffDate;
import com.solveria.payroll.domain.model.vo.PeriodStatus;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity: Periodo de Nómina — hijo del AR {@code PayrollPeriodConfig}.
 *
 * <p>Representa un periodo de nómina con mes, año, fecha de corte,
 * estado y referencia al calendario de feriados. Dominio puro.
 */
public class PayrollPeriod {

    private final UUID periodId;
    private final int month;
    private final int year;
    private final CutoffDate cutoffDate;
    private PeriodStatus status;
    private final UUID holidayCalendarRef;
    private final UUID tenantId;

    /**
     * Constructor de reconstrucción (desde persistencia).
     */
    public PayrollPeriod(
            UUID periodId,
            int month,
            int year,
            CutoffDate cutoffDate,
            PeriodStatus status,
            UUID holidayCalendarRef,
            UUID tenantId) {
        this.periodId = Objects.requireNonNull(periodId, "periodId es requerido");
        validateMonth(month);
        this.month = month;
        this.year = year;
        this.cutoffDate = Objects.requireNonNull(cutoffDate, "cutoffDate es requerido");
        this.status = Objects.requireNonNull(status, "status es requerido");
        this.holidayCalendarRef = holidayCalendarRef;
        this.tenantId = Objects.requireNonNull(tenantId, "tenantId es requerido");
    }

    /**
     * Factory: crea un nuevo periodo en estado {@code OPEN}.
     */
    public static PayrollPeriod create(
            int month,
            int year,
            LocalDate cutoffDate,
            UUID holidayCalendarRef,
            UUID tenantId) {
        return new PayrollPeriod(
                UUID.randomUUID(),
                month,
                year,
                new CutoffDate(cutoffDate),
                PeriodStatus.OPEN,
                holidayCalendarRef,
                tenantId);
    }

    /**
     * Cierra el periodo. Solo puede cerrarse si está en estado {@code OPEN}.
     */
    public void close() {
        if (this.status != PeriodStatus.OPEN) {
            throw new IllegalStateException(
                    "El periodo " + periodId + " no puede cerrarse; estado actual: " + status);
        }
        this.status = PeriodStatus.CLOSED;
    }

    private void validateMonth(int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Mes inválido: " + month + ". Rango permitido [1-12]");
        }
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public UUID getPeriodId() { return periodId; }
    public int getMonth() { return month; }
    public int getYear() { return year; }
    public CutoffDate getCutoffDate() { return cutoffDate; }
    public PeriodStatus getStatus() { return status; }
    public UUID getHolidayCalendarRef() { return holidayCalendarRef; }
    public UUID getTenantId() { return tenantId; }
}
