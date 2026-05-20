package com.solveria.payroll.infrastructure.adapter;

import com.solveria.payroll.application.port.outbound.DeductionRecordRepositoryPort;
import com.solveria.payroll.domain.model.ar.DeductionRecord;
import com.solveria.payroll.infrastructure.jpa.DeductionRecordJpa;
import com.solveria.payroll.infrastructure.mapper.DeductionRecordMapper;
import com.solveria.payroll.infrastructure.repository.DeductionRecordSpringRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adapter: Implementación del {@link DeductionRecordRepositoryPort}.
 *
 * <p>Persiste y consulta registros de egresos usando Spring Data JPA y el mapper bidireccional
 * Domain ↔ JPA.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeductionRecordRepositoryAdapter implements DeductionRecordRepositoryPort {

  private final DeductionRecordSpringRepository springRepository;
  private final DeductionRecordMapper mapper;

  @Override
  @Transactional
  public void save(DeductionRecord deductionRecord) {
    log.info(
        "event=PRL_DEDUCTION_RECORD_SAVE deductionRecordId={} employeeId={} deductionType={} tenantId={}",
        deductionRecord.getDeductionRecordId(),
        deductionRecord.getEmployeeId(),
        deductionRecord.getDeductionType(),
        deductionRecord.getTenantId());
    DeductionRecordJpa jpa = mapper.toJpa(deductionRecord);
    springRepository.save(jpa);
  }

  @Override
  public Optional<DeductionRecord> findById(UUID deductionRecordId, UUID tenantId) {
    return springRepository
        .findByDeductionRecordIdAndTenantId(deductionRecordId, tenantId)
        .map(mapper::toDomain);
  }

  @Override
  public List<DeductionRecord> findByEmployeeAndPeriod(
      UUID employeeId, UUID periodRef, UUID tenantId) {
    return springRepository
        .findAllByEmployeeIdAndPeriodRefAndTenantId(employeeId, periodRef, tenantId)
        .stream()
        .map(mapper::toDomain)
        .toList();
  }

  @Override
  public List<DeductionRecord> findAllByPeriod(UUID periodRef, UUID tenantId) {
    return springRepository.findAllByPeriodRefAndTenantId(periodRef, tenantId).stream()
        .map(mapper::toDomain)
        .toList();
  }
}
