package com.solveria.payroll.application.port.outbound;

import com.solveria.payroll.domain.model.entity.BankEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto Secundario (Outbound): Repositorio de BankEntity.
 *
 * <p>Contrato que la capa de infraestructura debe implementar
 * para persistir y consultar entidades bancarias.
 */
public interface BankEntityRepositoryPort {

    /**
     * Persiste una entidad bancaria.
     */
    void save(BankEntity bankEntity);

    /**
     * Busca una entidad bancaria por su ID y tenantId.
     */
    Optional<BankEntity> findById(UUID bankEntityId, String tenantId);

    /**
     * Lista todas las entidades bancarias de un tenant.
     */
    List<BankEntity> findAllByTenantId(String tenantId);

    /**
     * Busca una entidad bancaria por código y tenantId.
     */
    Optional<BankEntity> findByBankCode(String bankCode, String tenantId);
}
