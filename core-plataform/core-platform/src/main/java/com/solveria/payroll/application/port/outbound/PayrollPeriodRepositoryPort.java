package com.solveria.payroll.application.port.outbound;

import com.solveria.payroll.domain.model.entity.PayrollPeriod;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto Secundario (Outbound): Repositorio de PayrollPeriod.
 *
 * <p>Contrato que la capa de infraestructura debe implementar para persistir y consultar periodos
 * de nómina.
 */
public interface PayrollPeriodRepositoryPort {

  /** Persiste un periodo de nómina. */
  void save(PayrollPeriod period);

  /** Busca un periodo por su ID y tenantId. */
  Optional<PayrollPeriod> findById(UUID periodId, UUID tenantId);

  /** Lista todos los periodos de un tenant. */
  List<PayrollPeriod> findAllByTenantId(UUID tenantId);

  /** Busca un periodo por mes, año y tenantId. */
  Optional<PayrollPeriod> findByMonthAndYear(int month, int year, UUID tenantId);
}
