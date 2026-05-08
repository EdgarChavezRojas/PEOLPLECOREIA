package com.solveria.payroll.domain.model.ar;

import com.solveria.payroll.domain.model.vo.IncomeAmount;
import com.solveria.payroll.domain.model.vo.IncomeType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root: Registro de Ingreso del período.
 *
 * <p>Representa un concepto positivo que suma al Total Ganado de un empleado
 * para un período determinado (Workflow Fase 2). Puede ser manual (cargado
 * por el operador) o automático (generado por el sistema vía integración
 * con TimeAndBearings o cálculos periódicos).
 *
 * <p><b>Dominio puro:</b> Ninguna anotación de Spring ni JPA.
 *
 * <p><b>Invariante legal (Quinquenio):</b> Si el tipo es {@link IncomeType#QUINQUENIO}
 * y han transcurrido más de 30 días calendario sin pago, se aplica automáticamente
 * una multa del 30% sobre el monto total. Esta multa es irreversible (Workflow Fase 2.3).
 */
public class IncomeRecord {

    private final UUID incomeRecordId;
    private final UUID employeeId;
    private final UUID periodRef;
    private final IncomeType incomeType;
    private IncomeAmount amount;
    private final boolean isAutomatic;
    private final String tenantId;
    private boolean quinquenioPenaltyApplied;
    private LocalDate quinquenioEligibleSince;

    /**
     * Constructor de reconstrucción (desde persistencia).
     */
    public IncomeRecord(
            UUID incomeRecordId,
            UUID employeeId,
            UUID periodRef,
            IncomeType incomeType,
            IncomeAmount amount,
            boolean isAutomatic,
            String tenantId) {
        this.incomeRecordId = Objects.requireNonNull(incomeRecordId, "incomeRecordId es requerido");
        this.employeeId = Objects.requireNonNull(employeeId, "employeeId es requerido");
        this.periodRef = Objects.requireNonNull(periodRef, "periodRef es requerido");
        this.incomeType = Objects.requireNonNull(incomeType, "incomeType es requerido");
        this.amount = Objects.requireNonNull(amount, "amount es requerido");
        this.tenantId = Objects.requireNonNull(tenantId, "tenantId es requerido");
        this.isAutomatic = isAutomatic;
        this.quinquenioPenaltyApplied = false;
    }

    /**
     * Factory: crea un IncomeRecord de carga manual por el operador (Workflow Fase 2.1).
     *
     * @param employeeId id del empleado (Relationship)
     * @param periodRef  referencia al período activo
     * @param incomeType tipo de ingreso
     * @param amount     monto del ingreso
     * @param tenantId   identificador del tenant
     * @return nuevo IncomeRecord manual
     */
    public static IncomeRecord createManual(
            UUID employeeId,
            UUID periodRef,
            IncomeType incomeType,
            BigDecimal amount,
            String tenantId) {
        return new IncomeRecord(
                UUID.randomUUID(),
                employeeId,
                periodRef,
                incomeType,
                new IncomeAmount(amount),
                false,
                tenantId);
    }

    /**
     * Factory: crea un IncomeRecord automático generado por el sistema
     * (integración con TM o ingresos periódicos — Workflow Fase 2.2 / 2.3).
     *
     * @param employeeId id del empleado (Relationship)
     * @param periodRef  referencia al período activo
     * @param incomeType tipo de ingreso
     * @param amount     monto calculado automáticamente
     * @param tenantId   identificador del tenant
     * @return nuevo IncomeRecord automático
     */
    public static IncomeRecord createAutomatic(
            UUID employeeId,
            UUID periodRef,
            IncomeType incomeType,
            BigDecimal amount,
            String tenantId) {
        return new IncomeRecord(
                UUID.randomUUID(),
                employeeId,
                periodRef,
                incomeType,
                new IncomeAmount(amount),
                true,
                tenantId);
    }

    // ── Método de dominio: Invariante legal Quinquenio ──────────────────────

    /**
     * Aplica la multa automática del 30% sobre el monto del quinquenio si han
     * transcurrido más de 30 días calendario sin registro de pago.
     *
     * <p><b>Invariante legal:</b> "El plazo legal para el pago del quinquenio
     * tras la solicitud es de 30 días calendario. Al día 31 sin registro de pago,
     * el sistema aplica automáticamente una multa del 30% sobre el monto total.
     * Esta multa no puede ser revertida una vez disparada." (Workflow Fase 2.3)
     *
     * <p>Este método es idempotente: si la multa ya fue aplicada, no se aplica nuevamente.
     *
     * @param currentDate fecha actual del servidor para evaluar el plazo
     * @throws IllegalStateException si el IncomeRecord no es de tipo QUINQUENIO
     */
    public void applyQuinquenioPenaltyIfOverdue(LocalDate currentDate) {
        Objects.requireNonNull(currentDate, "currentDate es requerido");

        if (incomeType != IncomeType.QUINQUENIO) {
            throw new IllegalStateException(
                    "La penalización de quinquenio solo aplica a registros de tipo QUINQUENIO, " +
                            "pero este registro es de tipo " + incomeType);
        }

        if (quinquenioPenaltyApplied) {
            return; // Idempotente: la multa ya fue aplicada
        }

        if (quinquenioEligibleSince == null) {
            return; // No se ha definido la fecha de elegibilidad
        }

        long daysSinceEligible = ChronoUnit.DAYS.between(quinquenioEligibleSince, currentDate);

        if (daysSinceEligible > 30) {
            // Multa del 30% — irreversible
            BigDecimal penaltyRate = new BigDecimal("0.30");
            this.amount = amount.applyPenalty(penaltyRate);
            this.quinquenioPenaltyApplied = true;
        }
    }

    /**
     * Establece la fecha desde la cual el empleado es elegible para el quinquenio.
     * Utilizada para el cálculo del plazo de 30 días.
     *
     * @param eligibleSince fecha de elegibilidad (evento QUINQUENIO_ELIGIBILITY_REACHED)
     */
    public void setQuinquenioEligibleSince(LocalDate eligibleSince) {
        if (incomeType != IncomeType.QUINQUENIO) {
            throw new IllegalStateException(
                    "quinquenioEligibleSince solo puede definirse en registros de tipo QUINQUENIO");
        }
        this.quinquenioEligibleSince = Objects.requireNonNull(eligibleSince, "eligibleSince es requerido");
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public UUID getIncomeRecordId() { return incomeRecordId; }
    public UUID getEmployeeId() { return employeeId; }
    public UUID getPeriodRef() { return periodRef; }
    public IncomeType getIncomeType() { return incomeType; }
    public IncomeAmount getAmount() { return amount; }
    public boolean isAutomatic() { return isAutomatic; }
    public String getTenantId() { return tenantId; }
    public boolean isQuinquenioPenaltyApplied() { return quinquenioPenaltyApplied; }
    public LocalDate getQuinquenioEligibleSince() { return quinquenioEligibleSince; }
}
