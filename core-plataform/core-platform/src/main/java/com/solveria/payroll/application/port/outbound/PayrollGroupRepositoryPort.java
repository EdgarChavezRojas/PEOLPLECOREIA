package com.solveria.payroll.application.port.outbound;

import com.solveria.payroll.domain.model.entity.PayrollGroup;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto Secundario (Outbound): Repositorio de PayrollGroup.
 *
 * <p>Contrato que la capa de infraestructura debe implementar
 * para persistir y consultar grupos de nómina.
 */
public interface PayrollGroupRepositoryPort {

    /**
     * Persiste un grupo de nómina.
     */
    void save(PayrollGroup group);

    /**
     * Busca un grupo por su ID y tenantId.
     */
    Optional<PayrollGroup> findById(UUID groupId, String tenantId);

    /**
     * Lista todos los grupos de un tenant.
     */
    List<PayrollGroup> findAllByTenantId(String tenantId);

    /**
     * Busca un grupo por código y tenantId.
     */
    Optional<PayrollGroup> findByGroupCode(String groupCode, String tenantId);
}
