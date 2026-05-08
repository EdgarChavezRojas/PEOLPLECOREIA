package com.solveria.payroll.domain.model.entity;

import com.solveria.payroll.domain.model.vo.PayrollType;

import java.util.Objects;
import java.util.UUID;

/**
 * Entity: Grupo de Nómina — hijo del AR {@code PayrollGroupConfig}.
 *
 * <p>Clasifica empleados por tipo de planilla y grupo de cálculo.
 * Dominio puro.
 */
public class PayrollGroup {

    private final UUID groupId;
    private final String groupCode;
    private final PayrollType typeCode;
    private final String description;
    private final String tenantId;

    /**
     * Constructor de reconstrucción (desde persistencia).
     */
    public PayrollGroup(
            UUID groupId,
            String groupCode,
            PayrollType typeCode,
            String description,
            String tenantId) {
        this.groupId = Objects.requireNonNull(groupId, "groupId es requerido");
        this.groupCode = Objects.requireNonNull(groupCode, "groupCode es requerido");
        if (groupCode.isBlank()) {
            throw new IllegalArgumentException("groupCode no puede estar vacío");
        }
        this.typeCode = Objects.requireNonNull(typeCode, "typeCode es requerido");
        this.description = description;
        this.tenantId = Objects.requireNonNull(tenantId, "tenantId es requerido");
    }

    /**
     * Factory: crea un nuevo grupo de nómina.
     */
    public static PayrollGroup create(
            String groupCode,
            PayrollType typeCode,
            String description,
            String tenantId) {
        return new PayrollGroup(
                UUID.randomUUID(),
                groupCode,
                typeCode,
                description,
                tenantId);
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public UUID getGroupId() { return groupId; }
    public String getGroupCode() { return groupCode; }
    public PayrollType getTypeCode() { return typeCode; }
    public String getDescription() { return description; }
    public String getTenantId() { return tenantId; }
}
