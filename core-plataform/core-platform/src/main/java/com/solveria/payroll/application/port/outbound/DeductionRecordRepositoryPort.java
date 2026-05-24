package com.solveria.payroll.application.port.outbound;

import com.solveria.payroll.domain.model.ar.DeductionRecord;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto Secundario (Outbound): Repositorio de DeductionRecord.
 *
 * <p>Contrato que la capa de infraestructura debe implementar para persistir y consultar registros
 * de egresos/descuentos del período.
 */
public interface DeductionRecordRepositoryPort {

  /** Persiste un registro de egreso. */
  void save(DeductionRecord deductionRecord);

  /** Busca un registro de egreso por su ID y tenantId. */
  Optional<DeductionRecord> findById(UUID deductionRecordId, UUID tenantId);

  /** Lista todos los registros de egreso de un empleado en un período. */
  List<DeductionRecord> findByEmployeeAndPeriod(UUID employeeId, UUID periodRef, UUID tenantId);

  /** Lista todos los registros de egreso de un período para un tenant. */
  List<DeductionRecord> findAllByPeriod(UUID periodRef, UUID tenantId);
}
