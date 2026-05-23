package com.solveria.core.workforce.application.usecase;

import com.solveria.core.security.context.SecurityTenantContext;
import com.solveria.core.workforce.application.dto.PositionResponse;
import com.solveria.core.workforce.application.port.PositionRepositoryPort;
import com.solveria.core.workforce.domain.model.vo.PositionStatus;
import com.solveria.core.workforce.infrastructure.mapper.PositionMapper;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListPositionsUseCase {

  private final PositionRepositoryPort positionRepositoryPort;
  private final PositionMapper positionMapper;

  public Page<PositionResponse> execute(Pageable pageable, PositionStatus status) {
    UUID tenantId = UUID.fromString(SecurityTenantContext.getCurrentTenantId());
    Page<com.solveria.core.workforce.domain.model.Position> positions =
        status == null
            ? positionRepositoryPort.findByTenantId(tenantId, pageable)
            : positionRepositoryPort.findByTenantIdAndStatus(tenantId, status, pageable);
    return positions.map(positionMapper::toResponse);
  }
}

