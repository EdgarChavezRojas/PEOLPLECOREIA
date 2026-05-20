package com.solveria.payroll.infrastructure.adapter;

import com.solveria.payroll.application.port.outbound.PayrollPeriodRepositoryPort;
import com.solveria.payroll.domain.model.entity.PayrollPeriod;
import com.solveria.payroll.infrastructure.jpa.PayrollPeriodJpa;
import com.solveria.payroll.infrastructure.mapper.PayrollPeriodMapper;
import com.solveria.payroll.infrastructure.repository.PayrollPeriodSpringRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adapter: Implementación del {@link PayrollPeriodRepositoryPort}.
 *
 * <p>Persiste y consulta periodos de nómina usando Spring Data JPA y el mapper bidireccional Domain
 * ↔ JPA.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PayrollPeriodRepositoryAdapter implements PayrollPeriodRepositoryPort {

  private final PayrollPeriodSpringRepository springRepository;
  private final PayrollPeriodMapper mapper;

  @Override
  @Transactional
  public void save(PayrollPeriod period) {
    log.info(
        "event=PRL_PAYROLL_PERIOD_SAVE periodId={} tenantId={}",
        period.getPeriodId(),
        period.getTenantId());
    PayrollPeriodJpa jpa = mapper.toJpa(period);
    springRepository.save(jpa);
  }

  @Override
  public Optional<PayrollPeriod> findById(UUID periodId, UUID tenantId) {
    return springRepository.findByPeriodIdAndTenantId(periodId, tenantId).map(mapper::toDomain);
  }

  @Override
  public List<PayrollPeriod> findAllByTenantId(UUID tenantId) {
    return springRepository.findAllByTenantId(tenantId).stream().map(mapper::toDomain).toList();
  }

  @Override
  public Optional<PayrollPeriod> findByMonthAndYear(int month, int year, UUID tenantId) {
    return springRepository
        .findByMonthAndYearAndTenantId(month, year, tenantId)
        .map(mapper::toDomain);
  }
}
