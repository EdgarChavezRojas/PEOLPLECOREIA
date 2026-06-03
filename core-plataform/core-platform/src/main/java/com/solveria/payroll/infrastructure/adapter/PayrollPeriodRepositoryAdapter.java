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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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
  @Caching(
      evict = {
        @CacheEvict(value = "payrollPeriods", key = "#period.periodId + '-' + #period.tenantId"),
        @CacheEvict(
            value = "payrollPeriodsByMonthYear",
            key = "#period.month + '-' + #period.year + '-' + #period.tenantId")
      })
  public void save(PayrollPeriod period) {
    log.info(
        "event=PRL_PAYROLL_PERIOD_SAVE periodId={} tenantId={}",
        period.getPeriodId(),
        period.getTenantId());
    PayrollPeriodJpa jpa = mapper.toJpa(period);
    springRepository.save(jpa);
  }

  @Override
  @Cacheable(
      value = "payrollPeriods",
      key = "#periodId + '-' + #tenantId",
      unless = "#result == null")
  public Optional<PayrollPeriod> findById(UUID periodId, UUID tenantId) {
    return springRepository.findByPeriodIdAndTenantId(periodId, tenantId).map(mapper::toDomain);
  }

  @Override
  public List<PayrollPeriod> findAllByTenantId(UUID tenantId) {
    return springRepository.findAllByTenantId(tenantId).stream().map(mapper::toDomain).toList();
  }

  @Override
  @Cacheable(
      value = "payrollPeriodsByMonthYear",
      key = "#month + '-' + #year + '-' + #tenantId",
      unless = "#result == null")
  public Optional<PayrollPeriod> findByMonthAndYear(int month, int year, UUID tenantId) {
    return springRepository
        .findByMonthAndYearAndTenantId(month, year, tenantId)
        .map(mapper::toDomain);
  }
}
