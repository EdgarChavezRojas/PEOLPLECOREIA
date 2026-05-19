package com.solveria.core.workforce.infrastructure.adapter;

import com.solveria.core.security.context.SecurityTenantContext;
import com.solveria.core.shared.outbox.application.port.EventOutboxPort;
import com.solveria.core.workforce.application.port.PositionRepositoryPort;
import com.solveria.core.workforce.domain.model.Position;
import com.solveria.core.workforce.infrastructure.jpa.PositionJpa;
import com.solveria.core.workforce.infrastructure.mapper.PositionMapper;
import com.solveria.core.workforce.infrastructure.repository.PositionRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PositionRepositoryAdapter implements PositionRepositoryPort {

  private final PositionRepository positionRepository;
  private final PositionMapper positionMapper;
  private final EventOutboxPort eventOutboxPort;

  @Override
  @Transactional
  public Position save(Position position) {
    PositionJpa positionJpa = positionMapper.toJpa(position);
    PositionJpa savedPositionJpa = positionRepository.save(positionJpa);
    Position savedPosition = positionMapper.toDomain(savedPositionJpa);

    eventOutboxPort.publish(position.pullDomainEvents());

    return savedPosition;
  }

  @Override
  public Optional<Position> findByPositionIdAndTenantId(UUID positionId, UUID tenantId) {
    UUID currentTenantId = UUID.fromString(SecurityTenantContext.getCurrentTenantId());
    if (!currentTenantId.equals(tenantId)) {
      return Optional.empty();
    }
    return positionRepository
        .findByPositionIdAndTenantId(positionId, currentTenantId)
        .map(positionMapper::toDomain);
  }

  @Override
  public int countByJobIdAndTenantId(UUID jobId, UUID tenantId) {
    UUID currentTenantId = UUID.fromString(SecurityTenantContext.getCurrentTenantId());
    if (!currentTenantId.equals(tenantId)) {
      return 0;
    }
    return positionRepository.countByJobIdAndTenantId(jobId, currentTenantId);
  }
}
