package com.solveria.payroll.infrastructure.adapter;

import com.solveria.payroll.application.port.outbound.BankEntityRepositoryPort;
import com.solveria.payroll.domain.model.entity.BankEntity;
import com.solveria.payroll.infrastructure.jpa.BankEntityJpa;
import com.solveria.payroll.infrastructure.mapper.BankEntityMapper;
import com.solveria.payroll.infrastructure.repository.BankEntitySpringRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adapter: Implementación del {@link BankEntityRepositoryPort}.
 *
 * <p>Persiste y consulta entidades bancarias usando Spring Data JPA y el mapper bidireccional
 * Domain ↔ JPA.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BankEntityRepositoryAdapter implements BankEntityRepositoryPort {

  private final BankEntitySpringRepository springRepository;
  private final BankEntityMapper mapper;

  @Override
  @Transactional
  public void save(BankEntity bankEntity) {
    log.info(
        "event=PRL_BANK_ENTITY_SAVE bankEntityId={} tenantId={}",
        bankEntity.getBankEntityId(),
        bankEntity.getTenantId());
    BankEntityJpa jpa = mapper.toJpa(bankEntity);
    springRepository.save(jpa);
  }

  @Override
  public Optional<BankEntity> findById(UUID bankEntityId, UUID tenantId) {
    return springRepository
        .findByBankEntityIdAndTenantId(bankEntityId, tenantId)
        .map(mapper::toDomain);
  }

  @Override
  public List<BankEntity> findAllByTenantId(UUID tenantId) {
    return springRepository.findAllByTenantId(tenantId).stream().map(mapper::toDomain).toList();
  }

  @Override
  public Optional<BankEntity> findByBankCode(String bankCode, UUID tenantId) {
    return springRepository.findByBankCodeAndTenantId(bankCode, tenantId).map(mapper::toDomain);
  }
}
