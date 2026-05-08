package com.solveria.payroll.domain.model.ar;

import com.solveria.payroll.domain.model.entity.BankEntity;
import com.solveria.payroll.domain.model.vo.BankCode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Aggregate Root: Configuración de Entidades Bancarias.
 *
 * <p>Gestiona la colección de {@link BankEntity} para un tenant dado.
 * Invariante: no pueden existir dos entidades bancarias con el mismo
 * {@code BankCode} para el mismo tenant.
 *
 * <p><b>Dominio puro:</b> Ninguna anotación de Spring ni JPA.
 */
public class BankEntityConfig {

    private final UUID configId;
    private final String tenantId;
    private final List<BankEntity> bankEntities;

    /**
     * Constructor de reconstrucción (desde persistencia).
     */
    public BankEntityConfig(UUID configId, String tenantId, List<BankEntity> bankEntities) {
        this.configId = Objects.requireNonNull(configId, "configId es requerido");
        this.tenantId = Objects.requireNonNull(tenantId, "tenantId es requerido");
        this.bankEntities = new ArrayList<>(Objects.requireNonNullElse(bankEntities, List.of()));
    }

    /**
     * Factory: crea una nueva configuración vacía de entidades bancarias.
     */
    public static BankEntityConfig create(String tenantId) {
        return new BankEntityConfig(UUID.randomUUID(), tenantId, new ArrayList<>());
    }

    /**
     * Agrega una nueva entidad bancaria.
     * Invariante: bankCode debe ser único dentro del tenant.
     */
    public BankEntity addBankEntity(String bankCode, String name, String fileFormatSpec) {
        BankCode code = new BankCode(bankCode);
        boolean duplicateExists = bankEntities.stream()
                .anyMatch(b -> b.getBankCode().equals(code));
        if (duplicateExists) {
            throw new IllegalStateException(
                    "Ya existe una entidad bancaria con código '" + bankCode + "' para este tenant");
        }
        BankEntity entity = BankEntity.create(bankCode, name, fileFormatSpec, tenantId);
        bankEntities.add(entity);
        return entity;
    }

    /**
     * Busca una entidad bancaria por ID.
     */
    public Optional<BankEntity> findBankEntity(UUID bankEntityId) {
        return bankEntities.stream()
                .filter(b -> b.getBankEntityId().equals(bankEntityId))
                .findFirst();
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public UUID getConfigId() { return configId; }
    public String getTenantId() { return tenantId; }

    /** @return vista inmutable de las entidades bancarias. */
    public List<BankEntity> getBankEntities() {
        return Collections.unmodifiableList(bankEntities);
    }
}
