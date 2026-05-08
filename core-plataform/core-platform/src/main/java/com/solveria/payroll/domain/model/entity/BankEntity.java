package com.solveria.payroll.domain.model.entity;

import com.solveria.payroll.domain.model.vo.BankCode;

import java.util.Objects;
import java.util.UUID;

/**
 * Entity: Entidad Bancaria — hijo del AR {@code BankEntityConfig}.
 *
 * <p>Almacena la configuración de un banco para la generación de archivos
 * de dispersión de nómina. Dominio puro.
 */
public class BankEntity {

    private final UUID bankEntityId;
    private final BankCode bankCode;
    private final String name;
    private final String fileFormatSpec;
    private final String tenantId;

    /**
     * Constructor de reconstrucción (desde persistencia).
     */
    public BankEntity(
            UUID bankEntityId,
            BankCode bankCode,
            String name,
            String fileFormatSpec,
            String tenantId) {
        this.bankEntityId = Objects.requireNonNull(bankEntityId, "bankEntityId es requerido");
        this.bankCode = Objects.requireNonNull(bankCode, "bankCode es requerido");
        this.name = Objects.requireNonNull(name, "name es requerido");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name no puede estar vacío");
        }
        this.fileFormatSpec = fileFormatSpec;
        this.tenantId = Objects.requireNonNull(tenantId, "tenantId es requerido");
    }

    /**
     * Factory: crea una nueva entidad bancaria.
     */
    public static BankEntity create(
            String bankCode,
            String name,
            String fileFormatSpec,
            String tenantId) {
        return new BankEntity(
                UUID.randomUUID(),
                new BankCode(bankCode),
                name,
                fileFormatSpec,
                tenantId);
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public UUID getBankEntityId() { return bankEntityId; }
    public BankCode getBankCode() { return bankCode; }
    public String getName() { return name; }
    public String getFileFormatSpec() { return fileFormatSpec; }
    public String getTenantId() { return tenantId; }
}
