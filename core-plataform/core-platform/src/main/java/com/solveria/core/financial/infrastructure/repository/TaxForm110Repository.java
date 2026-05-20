package com.solveria.core.financial.infrastructure.repository;

import com.solveria.core.financial.infrastructure.jpa.TaxForm110Jpa;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA Repository: TaxForm110. */
@Repository
public interface TaxForm110Repository extends JpaRepository<TaxForm110Jpa, Long> {

  Optional<TaxForm110Jpa> findByFormIdAndTenantId(UUID formId, UUID tenantId);

  List<TaxForm110Jpa> findByPersonIdAndPeriodYearAndPeriodMonthAndTenantId(
      UUID personId, int periodYear, int periodMonth, UUID tenantId);
}
