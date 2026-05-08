package com.solveria.payroll.infrastructure.jpa;

import com.solveria.core.shared.base.BaseEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * JPA Entity: {@code prl_income_record} — Persistencia del Registro de Ingreso.
 *
 * <p>Extiende {@link BaseEntity} para herencia de {@code tenant_id}, {@code created_at},
 * {@code created_by}, {@code last_modified_at}, {@code last_modified_by} y {@code version}.
 *
 * <p>Mapea el TOON: prl_income_record (id UUID PK, employee_id UUID, period_ref UUID,
 * income_type String, amount BigDecimal, is_automatic Boolean, tenant_id String).
 */
@Entity
@Table(name = "prl_income_record")
public class IncomeRecordJpa extends BaseEntity {

    @Column(name = "income_record_id", nullable = false, unique = true, updatable = false,
            columnDefinition = "UUID")
    private UUID incomeRecordId;

    @Column(name = "employee_id", nullable = false, columnDefinition = "UUID")
    private UUID employeeId;

    @Column(name = "period_ref", nullable = false, columnDefinition = "UUID")
    private UUID periodRef;

    @Column(name = "income_type", nullable = false, length = 50)
    private String incomeType;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "is_automatic", nullable = false)
    private Boolean isAutomatic;

    public IncomeRecordJpa() {
        // JPA
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public UUID getIncomeRecordId() { return incomeRecordId; }
    public void setIncomeRecordId(UUID incomeRecordId) { this.incomeRecordId = incomeRecordId; }

    public UUID getEmployeeId() { return employeeId; }
    public void setEmployeeId(UUID employeeId) { this.employeeId = employeeId; }

    public UUID getPeriodRef() { return periodRef; }
    public void setPeriodRef(UUID periodRef) { this.periodRef = periodRef; }

    public String getIncomeType() { return incomeType; }
    public void setIncomeType(String incomeType) { this.incomeType = incomeType; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public Boolean getIsAutomatic() { return isAutomatic; }
    public void setIsAutomatic(Boolean isAutomatic) { this.isAutomatic = isAutomatic; }
}
