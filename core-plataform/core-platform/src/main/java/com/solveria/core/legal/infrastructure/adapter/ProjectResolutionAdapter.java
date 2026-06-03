package com.solveria.core.legal.infrastructure.adapter;

import com.solveria.core.financial.application.port.FundingSourceRepositoryPort;
import com.solveria.core.financial.domain.model.FundingSource;
import com.solveria.core.legal.application.port.ProjectResolutionPort;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectResolutionAdapter implements ProjectResolutionPort {

  private final FundingSourceRepositoryPort fundingSourceRepositoryPort;

  @Override
  public UUID getDefaultProjectIdForTenant(UUID tenantId) {
    List<FundingSource> sources = fundingSourceRepositoryPort.findAllByTenantId(tenantId);
    if (sources.isEmpty()) {
      throw new IllegalStateException(
          "No se encontró ningún FundingSource/Project configurado para el tenant: " + tenantId);
    }
    // Retornamos el primer proyecto como proyecto por defecto para el tenant
    return sources.get(0).getSourceId();
  }
}
