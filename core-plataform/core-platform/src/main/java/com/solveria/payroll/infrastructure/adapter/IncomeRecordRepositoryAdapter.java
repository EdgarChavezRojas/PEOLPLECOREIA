package com.solveria.payroll.infrastructure.adapter;

import com.solveria.payroll.application.port.outbound.IncomeRecordRepositoryPort;
import com.solveria.payroll.domain.model.ar.IncomeRecord;
import com.solveria.payroll.infrastructure.jpa.IncomeRecordJpa;
import com.solveria.payroll.infrastructure.mapper.IncomeRecordMapper;
import com.solveria.payroll.infrastructure.repository.IncomeRecordSpringRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter: Implementación del {@link IncomeRecordRepositoryPort}.
 *
 * <p>Persiste y consulta registros de ingresos usando Spring Data JPA
 * y el mapper bidireccional Domain ↔ JPA.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IncomeRecordRepositoryAdapter implements IncomeRecordRepositoryPort {

    private final IncomeRecordSpringRepository springRepository;
    private final IncomeRecordMapper mapper;

    @Override
    @Transactional
    public void save(IncomeRecord incomeRecord) {
        log.info("event=PRL_INCOME_RECORD_SAVE incomeRecordId={} employeeId={} incomeType={} tenantId={}",
                incomeRecord.getIncomeRecordId(), incomeRecord.getEmployeeId(),
                incomeRecord.getIncomeType(), incomeRecord.getTenantId());
        IncomeRecordJpa jpa = mapper.toJpa(incomeRecord);
        springRepository.save(jpa);
    }

    @Override
    public Optional<IncomeRecord> findById(UUID incomeRecordId, String tenantId) {
        return springRepository.findByIncomeRecordIdAndTenantId(incomeRecordId, tenantId)
                .map(mapper::toDomain);
    }

    @Override
    public List<IncomeRecord> findByEmployeeAndPeriod(UUID employeeId, UUID periodRef, String tenantId) {
        return springRepository.findAllByEmployeeIdAndPeriodRefAndTenantId(employeeId, periodRef, tenantId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<IncomeRecord> findAllByPeriod(UUID periodRef, String tenantId) {
        return springRepository.findAllByPeriodRefAndTenantId(periodRef, tenantId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
