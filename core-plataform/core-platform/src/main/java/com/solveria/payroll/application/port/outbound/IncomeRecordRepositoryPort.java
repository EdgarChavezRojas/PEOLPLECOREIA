package com.solveria.payroll.application.port.outbound;

import com.solveria.payroll.domain.model.ar.IncomeRecord;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto Secundario (Outbound): Repositorio de IncomeRecord.
 *
 * <p>Contrato que la capa de infraestructura debe implementar
 * para persistir y consultar registros de ingresos del período.
 */
public interface IncomeRecordRepositoryPort {

    /**
     * Persiste un registro de ingreso.
     */
    void save(IncomeRecord incomeRecord);

    /**
     * Busca un registro de ingreso por su ID y tenantId.
     */
    Optional<IncomeRecord> findById(UUID incomeRecordId, String tenantId);

    /**
     * Lista todos los registros de ingreso de un empleado en un período.
     */
    List<IncomeRecord> findByEmployeeAndPeriod(UUID employeeId, UUID periodRef, String tenantId);

    /**
     * Lista todos los registros de ingreso de un período para un tenant.
     */
    List<IncomeRecord> findAllByPeriod(UUID periodRef, String tenantId);
}
