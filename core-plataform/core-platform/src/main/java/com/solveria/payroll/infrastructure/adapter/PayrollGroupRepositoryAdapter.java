package com.solveria.payroll.infrastructure.adapter;

import com.solveria.payroll.application.port.outbound.PayrollGroupRepositoryPort;
import com.solveria.payroll.domain.model.entity.PayrollGroup;
import com.solveria.payroll.infrastructure.jpa.PayrollGroupJpa;
import com.solveria.payroll.infrastructure.mapper.PayrollGroupMapper;
import com.solveria.payroll.infrastructure.repository.PayrollGroupSpringRepository;
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
 * Adapter: Implementación del {@link PayrollGroupRepositoryPort}.
 *
 * <p>
 * Persiste y consulta grupos de nómina usando Spring Data JPA y el mapper
 * bidireccional Domain ↔
 * JPA.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PayrollGroupRepositoryAdapter implements PayrollGroupRepositoryPort {

  private final PayrollGroupSpringRepository springRepository;
  private final PayrollGroupMapper mapper;

  @Override
  @Transactional
  @Caching(evict = {
      @CacheEvict(value = "payrollGroups", key = "#group.groupId + '-' + #group.tenantId"),
      @CacheEvict(value = "payrollGroupsByCode", key = "#group.groupCode + '-' + #group.tenantId")
  })
  public void save(PayrollGroup group) {
    log.info(
        "event=PRL_PAYROLL_GROUP_SAVE groupId={} tenantId={}",
        group.getGroupId(),
        group.getTenantId());
    PayrollGroupJpa jpa = mapper.toJpa(group);
    springRepository.save(jpa);
  }

  @Override
  @Cacheable(value = "payrollGroups", key = "#groupId + '-' + #tenantId", unless = "#result == null")
  public Optional<PayrollGroup> findById(UUID groupId, UUID tenantId) {
    return springRepository.findByGroupIdAndTenantId(groupId, tenantId).map(mapper::toDomain);
  }

  @Override
  public List<PayrollGroup> findAllByTenantId(UUID tenantId) {
    return springRepository.findAllByTenantId(tenantId).stream().map(mapper::toDomain).toList();
  }

  @Override
  @Cacheable(value = "payrollGroupsByCode", key = "#groupCode + '-' + #tenantId", unless = "#result == null")
  public Optional<PayrollGroup> findByGroupCode(String groupCode, UUID tenantId) {
    return springRepository.findByGroupCodeAndTenantId(groupCode, tenantId).map(mapper::toDomain);
  }
}
