package com.solveria.payroll.infrastructure.adapter;

import com.solveria.payroll.application.port.outbound.PayrollGroupRepositoryPort;
import com.solveria.payroll.domain.model.entity.PayrollGroup;
import com.solveria.payroll.infrastructure.jpa.PayrollGroupJpa;
import com.solveria.payroll.infrastructure.mapper.PayrollGroupMapper;
import com.solveria.payroll.infrastructure.repository.PayrollGroupSpringRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter: Implementación del {@link PayrollGroupRepositoryPort}.
 *
 * <p>Persiste y consulta grupos de nómina usando Spring Data JPA
 * y el mapper bidireccional Domain ↔ JPA.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PayrollGroupRepositoryAdapter implements PayrollGroupRepositoryPort {

    private final PayrollGroupSpringRepository springRepository;
    private final PayrollGroupMapper mapper;

    @Override
    @Transactional
    public void save(PayrollGroup group) {
        log.info("event=PRL_PAYROLL_GROUP_SAVE groupId={} tenantId={}",
                group.getGroupId(), group.getTenantId());
        PayrollGroupJpa jpa = mapper.toJpa(group);
        springRepository.save(jpa);
    }

    @Override
    public Optional<PayrollGroup> findById(UUID groupId, String tenantId) {
        return springRepository.findByGroupIdAndTenantId(groupId, tenantId)
                .map(mapper::toDomain);
    }

    @Override
    public List<PayrollGroup> findAllByTenantId(String tenantId) {
        return springRepository.findAllByTenantId(tenantId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<PayrollGroup> findByGroupCode(String groupCode, String tenantId) {
        return springRepository.findByGroupCodeAndTenantId(groupCode, tenantId)
                .map(mapper::toDomain);
    }
}
