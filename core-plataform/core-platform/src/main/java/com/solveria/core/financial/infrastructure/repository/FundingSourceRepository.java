package com.solveria.core.financial.infrastructure.repository;

import com.solveria.core.financial.infrastructure.jpa.FundingSourceJpa;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA Repository: FundingSource. */
@Repository
public interface FundingSourceRepository extends JpaRepository<FundingSourceJpa, UUID> {

  Optional<FundingSourceJpa> findBySourceIdAndTenantId(UUID sourceId, UUID tenantId);

  Optional<FundingSourceJpa> findByProjectCodeAndTenantId(String projectCode, UUID tenantId);

  List<FundingSourceJpa> findAllByTenantId(UUID tenantId);
}
