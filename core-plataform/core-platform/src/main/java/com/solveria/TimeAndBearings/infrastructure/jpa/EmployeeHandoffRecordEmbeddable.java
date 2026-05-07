package com.solveria.TimeAndBearings.infrastructure.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * JPA Embeddable: persiste el VO {@code EmployeeHandoffRecord} como parte de la
 * colección {@code @ElementCollection} de {@link PayrollHandoffPackageJpa}.
 *
 * <p>Tabla destino: {@code payroll_handoff_employee_record}.
 * Columna de unión: {@code handoff_id} (FK a {@code payroll_handoff_package.handoff_id}).
 *
 * <p>PK compuesto de la tabla: {@code (handoff_id, relationship_id)}.
 *
 * <p>Todos los campos son inmutables una vez persistidos (P-TM33): el
 * {@code PayrollHandoffPackage} es el contrato sellado hacia BC-05.
 */
@Embeddable
public class EmployeeHandoffRecordEmbeddable {

    @Column(name = "relationship_id", nullable = false, columnDefinition = "UUID")
    private UUID relationshipId;

    @Column(name = "regular_hours_total", nullable = false, precision = 8, scale = 2)
    private BigDecimal regularHoursTotal;

    @Column(name = "overtime_hours_total", nullable = false, precision = 8, scale = 2)
    private BigDecimal overtimeHoursTotal;

    @Column(name = "night_hours_total", nullable = false, precision = 8, scale = 2)
    private BigDecimal nightHoursTotal;

    @Column(name = "holiday_hours_total", nullable = false, precision = 8, scale = 2)
    private BigDecimal holidayHoursTotal;

    /** Número de días con ausencia injustificada. Impacta descuento en nómina. */
    @Column(name = "unjustified_absences", nullable = false)
    private int unjustifiedAbsences;

    /** Número de días trabajados en modalidad remota autorizada. */
    @Column(name = "remote_work_days", nullable = false)
    private int remoteWorkDays;

    /**
     * Indicador de calidad de datos: COMPLETE o PARTIAL_AUTO_CLOSED (P-TM31/P-TM34).
     * Almacenado como STRING para legibilidad en BD y compatibilidad con evoluciones.
     */
    @Column(name = "data_quality_flag", nullable = false, length = 30)
    private String dataQualityFlag;

    public EmployeeHandoffRecordEmbeddable() {
        // JPA
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public UUID getRelationshipId() { return relationshipId; }
    public void setRelationshipId(UUID relationshipId) { this.relationshipId = relationshipId; }

    public BigDecimal getRegularHoursTotal() { return regularHoursTotal; }
    public void setRegularHoursTotal(BigDecimal regularHoursTotal) {
        this.regularHoursTotal = regularHoursTotal;
    }

    public BigDecimal getOvertimeHoursTotal() { return overtimeHoursTotal; }
    public void setOvertimeHoursTotal(BigDecimal overtimeHoursTotal) {
        this.overtimeHoursTotal = overtimeHoursTotal;
    }

    public BigDecimal getNightHoursTotal() { return nightHoursTotal; }
    public void setNightHoursTotal(BigDecimal nightHoursTotal) {
        this.nightHoursTotal = nightHoursTotal;
    }

    public BigDecimal getHolidayHoursTotal() { return holidayHoursTotal; }
    public void setHolidayHoursTotal(BigDecimal holidayHoursTotal) {
        this.holidayHoursTotal = holidayHoursTotal;
    }

    public int getUnjustifiedAbsences() { return unjustifiedAbsences; }
    public void setUnjustifiedAbsences(int unjustifiedAbsences) {
        this.unjustifiedAbsences = unjustifiedAbsences;
    }

    public int getRemoteWorkDays() { return remoteWorkDays; }
    public void setRemoteWorkDays(int remoteWorkDays) { this.remoteWorkDays = remoteWorkDays; }

    public String getDataQualityFlag() { return dataQualityFlag; }
    public void setDataQualityFlag(String dataQualityFlag) {
        this.dataQualityFlag = dataQualityFlag;
    }
}
