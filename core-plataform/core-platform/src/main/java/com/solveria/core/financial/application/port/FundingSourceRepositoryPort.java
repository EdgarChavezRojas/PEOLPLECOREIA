package com.solveria.core.financial.application.port;

import com.solveria.core.financial.domain.model.FundingSource;
import java.util.Optional;
import java.util.UUID;

/** Secondary Port (Outbound): Repositorio de FundingSource. */
public interface FundingSourceRepositoryPort {

  Optional<FundingSource> findById(UUID sourceId);

  Optional<FundingSource> findByProjectCode(String projectCode, String tenantId);

  void save(FundingSource fundingSource);
}
