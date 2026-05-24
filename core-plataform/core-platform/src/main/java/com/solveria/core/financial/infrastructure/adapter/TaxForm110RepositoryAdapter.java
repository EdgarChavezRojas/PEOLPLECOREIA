package com.solveria.core.financial.infrastructure.adapter;

import com.solveria.core.financial.application.port.TaxForm110RepositoryPort;
import com.solveria.core.financial.domain.model.TaxForm110;
import com.solveria.core.financial.infrastructure.mapper.TaxForm110Mapper;
import com.solveria.core.financial.infrastructure.repository.TaxForm110Repository;
import com.solveria.core.security.context.SecurityTenantContext;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Adapter: TaxForm110RepositoryPort. */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaxForm110RepositoryAdapter implements TaxForm110RepositoryPort {

  private final TaxForm110Repository taxForm110Repository;
  private final TaxForm110Mapper taxForm110Mapper;

  @Override
  @Transactional
  public void save(TaxForm110 form) {
    taxForm110Repository.save(taxForm110Mapper.toJpa(form));
  }

  @Override
  public Optional<TaxForm110> findById(UUID formId) {
    UUID currentTenantId = UUID.fromString(SecurityTenantContext.getCurrentTenantId());
    return taxForm110Repository
        .findByFormIdAndTenantId(formId, currentTenantId)
        .map(taxForm110Mapper::toDomain);
  }

  @Override
  public List<TaxForm110> findByPersonIdAndPeriod(
      UUID personId, YearMonth period, String tenantId) {
    UUID resolvedTenantId =
        UUID.fromString(tenantId != null ? tenantId : SecurityTenantContext.getCurrentTenantId());
    return taxForm110Repository
        .findByPersonIdAndPeriodYearAndPeriodMonthAndTenantId(
            personId, period.getYear(), period.getMonthValue(), resolvedTenantId)
        .stream()
        .map(taxForm110Mapper::toDomain)
        .toList();
  }
}
