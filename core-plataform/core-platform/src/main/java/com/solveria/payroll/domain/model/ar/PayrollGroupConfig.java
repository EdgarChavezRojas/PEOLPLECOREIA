package com.solveria.payroll.domain.model.ar;

import com.solveria.payroll.domain.model.entity.PayrollGroup;
import com.solveria.payroll.domain.model.vo.PayrollType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Aggregate Root: Configuración de Grupos de Nómina.
 *
 * <p>Gestiona la colección de {@link PayrollGroup} para un tenant dado.
 * Invariante: no pueden existir dos grupos con el mismo {@code groupCode}
 * para el mismo tenant.
 *
 * <p><b>Dominio puro:</b> Ninguna anotación de Spring ni JPA.
 */
public class PayrollGroupConfig {

    private final UUID configId;
    private final String tenantId;
    private final List<PayrollGroup> groups;

    /**
     * Constructor de reconstrucción (desde persistencia).
     */
    public PayrollGroupConfig(UUID configId, String tenantId, List<PayrollGroup> groups) {
        this.configId = Objects.requireNonNull(configId, "configId es requerido");
        this.tenantId = Objects.requireNonNull(tenantId, "tenantId es requerido");
        this.groups = new ArrayList<>(Objects.requireNonNullElse(groups, List.of()));
    }

    /**
     * Factory: crea una nueva configuración vacía de grupos.
     */
    public static PayrollGroupConfig create(String tenantId) {
        return new PayrollGroupConfig(UUID.randomUUID(), tenantId, new ArrayList<>());
    }

    /**
     * Agrega un nuevo grupo de nómina.
     * Invariante: groupCode debe ser único dentro del tenant.
     */
    public PayrollGroup addGroup(String groupCode, PayrollType typeCode, String description) {
        boolean duplicateExists = groups.stream()
                .anyMatch(g -> g.getGroupCode().equals(groupCode));
        if (duplicateExists) {
            throw new IllegalStateException(
                    "Ya existe un grupo con código '" + groupCode + "' para este tenant");
        }
        PayrollGroup group = PayrollGroup.create(groupCode, typeCode, description, tenantId);
        groups.add(group);
        return group;
    }

    /**
     * Busca un grupo por ID.
     */
    public Optional<PayrollGroup> findGroup(UUID groupId) {
        return groups.stream()
                .filter(g -> g.getGroupId().equals(groupId))
                .findFirst();
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public UUID getConfigId() { return configId; }
    public String getTenantId() { return tenantId; }

    /** @return vista inmutable de los grupos. */
    public List<PayrollGroup> getGroups() {
        return Collections.unmodifiableList(groups);
    }
}
