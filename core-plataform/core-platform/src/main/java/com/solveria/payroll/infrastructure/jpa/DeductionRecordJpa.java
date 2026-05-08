package com.solveria.payroll.infrastructure.jpa;

import com.solveria.core.shared.base.BaseEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * JPA Entity: {@code prl_deduction_record} — Persistencia del Registro de Egreso.
 *
 * <p>Extiende {@link BaseEntity} para herencia de {@code tenant_id}, {@code created_at},
 * {@code created_by}, {@code last_modified_at}, {@code last_modified_by} y {@code version}.
 *
 * <p>Mapea el TOON: prl_deduction_record (id UUID PK, employee_id UUID, period_ref UUID,
 * deduction_type String, amount BigDecimal, is_automatic Boolean, tenant_id String).
 */
@Entity
@Table(name = "prl_deduction_record")
public class DeductionRecordJpa extends BaseEntity {

    @Column(name = "deduction_record_id", nullable = false, unique = true, updatable = false,
            columnDefinition = "UUID")
    private UUID deductionRecordId;

    @Column(name = "employee_id", nullable = false, columnDefinition = "UUID")
    private UUID employeeId;

    @Column(name = "period_ref", nullable = false, columnDefinition = "UUID")
    private UUID periodRef;

    @Column(name = "deduction_type", nullable = false, length = 50)
    private String deductionType;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "is_automatic", nullable = false)
    private Boolean isAutomatic;

    public DeductionRecordJpa() {
        // JPA
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public UUID getDeductionRecordId() { return deductionRecordId; }
    public void setDeductionRecordId(UUID deductionRecordId) { this.deductionRecordId = deductionRecordId; }

    public UUID getEmployeeId() { return employeeId; }
    public void setEmployeeId(UUID employeeId) { this.employeeId = employeeId; }

    public UUID getPeriodRef() { return periodRef; }
    public void setPeriodRef(UUID periodRef) { this.periodRef = periodRef; }

    public String getDeductionType() { return deductionType; }
    public void setDeductionType(String deductionType) { this.deductionType = deductionType; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public Boolean getIsAutomatic() { return isAutomatic; }
    public void setIsAutomatic(Boolean isAutomatic) { this.isAutomatic = isAutomatic; }
}
