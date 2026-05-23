package com.solveria.core.workforce.application.usecase;

import com.solveria.core.security.context.SecurityTenantContext;
import com.solveria.core.workforce.application.dto.OrgUnitResponse;
import com.solveria.core.workforce.application.port.OrgUnitRepositoryPort;
import com.solveria.core.workforce.infrastructure.mapper.OrgUnitMapper;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListOrgUnitsUseCase {

  private final OrgUnitRepositoryPort orgUnitRepositoryPort;
  private final OrgUnitMapper orgUnitMapper;

  public Page<OrgUnitResponse> execute(Pageable pageable) {
    UUID tenantId = UUID.fromString(SecurityTenantContext.getCurrentTenantId());
    return orgUnitRepositoryPort.findByTenantId(tenantId, pageable).map(orgUnitMapper::toResponse);
  }
}

