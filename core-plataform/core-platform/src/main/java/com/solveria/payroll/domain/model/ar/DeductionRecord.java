package com.solveria.payroll.domain.model.ar;

import com.solveria.payroll.domain.model.vo.DeductionAmount;
import com.solveria.payroll.domain.model.vo.DeductionType;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root: Registro de Egreso/Descuento del período.
 *
 * <p>Representa un concepto que reduce el Total Ganado de un empleado para
 * obtener el Líquido Pagable (Workflow Fase 3). Puede ser manual (cargado
 * por el operador) o automático (generado por el sistema vía integración
 * con TimeAndBearings o descuentos periódicos).
 *
 * <p><b>Dominio puro:</b> Ninguna anotación de Spring ni JPA.
 *
 * <p><b>Regla crítica:</b> Un anticipo solo puede cargarse para el período
 * activo, nunca para períodos cerrados. Esta validación se realiza en la
 * capa de aplicación (Use Case) antes de invocar al factory.
 */
public class DeductionRecord {

    private final UUID deductionRecordId;
    private final UUID employeeId;
    private final UUID periodRef;
    private final DeductionType deductionType;
    private final DeductionAmount amount;
    private final boolean isAutomatic;
    private final String tenantId;

    /**
     * Constructor de reconstrucción (desde persistencia).
     */
    public DeductionRecord(
            UUID deductionRecordId,
            UUID employeeId,
            UUID periodRef,
            DeductionType deductionType,
            DeductionAmount amount,
            boolean isAutomatic,
            String tenantId) {
        this.deductionRecordId = Objects.requireNonNull(deductionRecordId, "deductionRecordId es requerido");
        this.employeeId = Objects.requireNonNull(employeeId, "employeeId es requerido");
        this.periodRef = Objects.requireNonNull(periodRef, "periodRef es requerido");
        this.deductionType = Objects.requireNonNull(deductionType, "deductionType es requerido");
        this.amount = Objects.requireNonNull(amount, "amount es requerido");
        this.tenantId = Objects.requireNonNull(tenantId, "tenantId es requerido");
        this.isAutomatic = isAutomatic;
    }

    /**
     * Factory: crea un DeductionRecord de carga manual por el operador (Workflow Fase 3.1).
     *
     * @param employeeId    id del empleado (Relationship)
     * @param periodRef     referencia al período activo
     * @param deductionType tipo de descuento
     * @param amount        monto del descuento
     * @param tenantId      identificador del tenant
     * @return nuevo DeductionRecord manual
     */
    public static DeductionRecord createManual(
            UUID employeeId,
            UUID periodRef,
            DeductionType deductionType,
            BigDecimal amount,
            String tenantId) {
        return new DeductionRecord(
                UUID.randomUUID(),
                employeeId,
                periodRef,
                deductionType,
                new DeductionAmount(amount),
                false,
                tenantId);
    }

    /**
     * Factory: crea un DeductionRecord automático generado por el sistema
     * (integración con TM para atrasos/ausencias o descuentos periódicos — Workflow Fase 3.2 / 3.3).
     *
     * @param employeeId    id del empleado (Relationship)
     * @param periodRef     referencia al período activo
     * @param deductionType tipo de descuento
     * @param amount        monto calculado automáticamente
     * @param tenantId      identificador del tenant
     * @return nuevo DeductionRecord automático
     */
    public static DeductionRecord createAutomatic(
            UUID employeeId,
            UUID periodRef,
            DeductionType deductionType,
            BigDecimal amount,
            String tenantId) {
        return new DeductionRecord(
                UUID.randomUUID(),
                employeeId,
                periodRef,
                deductionType,
                new DeductionAmount(amount),
                true,
                tenantId);
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public UUID getDeductionRecordId() { return deductionRecordId; }
    public UUID getEmployeeId() { return employeeId; }
    public UUID getPeriodRef() { return periodRef; }
    public DeductionType getDeductionType() { return deductionType; }
    public DeductionAmount getAmount() { return amount; }
    public boolean isAutomatic() { return isAutomatic; }
    public String getTenantId() { return tenantId; }
}
