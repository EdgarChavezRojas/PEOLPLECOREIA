package com.solveria.core.workforce.application.usecase;

import com.solveria.core.security.context.SecurityTenantContext;
import com.solveria.core.workforce.application.port.OrgUnitRepositoryPort;
import com.solveria.core.workforce.application.port.PositionRepositoryPort;
import com.solveria.core.workforce.application.dto.CreatePositionRequest;
import com.solveria.core.workforce.application.dto.PositionResponse;
import com.solveria.core.workforce.domain.exception.OrgUnitNotFoundException;
import com.solveria.core.workforce.domain.model.Position;
import com.solveria.core.workforce.infrastructure.mapper.PositionMapper;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreatePositionUseCase {

  private final PositionRepositoryPort positionRepositoryPort;
  private final OrgUnitRepositoryPort orgUnitRepositoryPort;
  private final PositionMapper positionMapper;

  @Transactional
  public PositionResponse execute(CreatePositionRequest request) {
    UUID tenantId = UUID.fromString(SecurityTenantContext.getCurrentTenantId());

    orgUnitRepositoryPort
        .findByUnitIdAndTenantId(request.getUnitId(), tenantId)
        .orElseThrow(
            () ->
                new OrgUnitNotFoundException("OrgUnit no encontrado o no pertenece a este tenant"));

    Position position =
        Position.create(
            request.getUnitId(),
            request.getJobId(),
            request.getIsBudgeted(),
            request.getMaxSlots());

    Position savedPosition = positionRepositoryPort.save(position);
    return positionMapper.toResponse(savedPosition);
  }
}
