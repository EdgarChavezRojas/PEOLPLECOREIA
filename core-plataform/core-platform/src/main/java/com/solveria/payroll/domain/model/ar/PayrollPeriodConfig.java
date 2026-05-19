package com.solveria.payroll.domain.model.ar;

import com.solveria.payroll.domain.model.entity.PayrollPeriod;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Aggregate Root: Configuración de Periodos de Nómina.
 *
 * <p>Gestiona la colección de {@link PayrollPeriod} para un tenant dado.
 * Garantiza consistencia transaccional de los periodos bajo su frontera.
 *
 * <p><b>Dominio puro:</b> Ninguna anotación de Spring ni JPA.
 */
public class PayrollPeriodConfig {

    private final UUID configId;
    private final UUID tenantId;
    private final List<PayrollPeriod> periods;

    /**
     * Constructor de reconstrucción (desde persistencia).
     */
    public PayrollPeriodConfig(UUID configId, UUID tenantId, List<PayrollPeriod> periods) {
        this.configId = Objects.requireNonNull(configId, "configId es requerido");
        this.tenantId = Objects.requireNonNull(tenantId, "tenantId es requerido");
        this.periods = new ArrayList<>(Objects.requireNonNullElse(periods, List.of()));
    }

    /**
     * Factory: crea una nueva configuración vacía de periodos.
     */
    public static PayrollPeriodConfig create(UUID tenantId) {
        return new PayrollPeriodConfig(UUID.randomUUID(), tenantId, new ArrayList<>());
    }

    /**
     * Agrega un nuevo periodo de nómina a la configuración.
     * Invariante: no pueden existir dos periodos con el mismo (month, year) para el mismo tenant.
     */
    public PayrollPeriod addPeriod(
            int month,
            int year,
            LocalDate cutoffDate,
            UUID holidayCalendarRef) {
        boolean duplicateExists = periods.stream()
                .anyMatch(p -> p.getMonth() == month && p.getYear() == year);
        if (duplicateExists) {
            throw new IllegalStateException(
                    "Ya existe un periodo para el mes " + month + " del año " + year);
        }
        PayrollPeriod period = PayrollPeriod.create(month, year, cutoffDate, holidayCalendarRef, tenantId);
        periods.add(period);
        return period;
    }

    /**
     * Cierra un periodo identificado por su ID.
     */
    public void closePeriod(UUID periodId) {
        PayrollPeriod period = findPeriodOrThrow(periodId);
        period.close();
    }

    /**
     * Busca un periodo por ID.
     */
    public Optional<PayrollPeriod> findPeriod(UUID periodId) {
        return periods.stream()
                .filter(p -> p.getPeriodId().equals(periodId))
                .findFirst();
    }

    private PayrollPeriod findPeriodOrThrow(UUID periodId) {
        return findPeriod(periodId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Periodo no encontrado: " + periodId));
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public UUID getConfigId() { return configId; }
    public UUID getTenantId() { return tenantId; }

    /** @return vista inmutable de los periodos. */
    public List<PayrollPeriod> getPeriods() {
        return Collections.unmodifiableList(periods);
    }
}
