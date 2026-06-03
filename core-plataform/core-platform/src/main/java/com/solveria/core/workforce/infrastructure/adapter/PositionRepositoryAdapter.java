package com.solveria.core.workforce.infrastructure.adapter;

import com.solveria.core.security.context.SecurityTenantContext;
import com.solveria.core.shared.outbox.application.port.EventOutboxPort;
import com.solveria.core.shared.pagination.PageUtils;
import com.solveria.core.workforce.application.port.PositionRepositoryPort;
import com.solveria.core.workforce.domain.model.Position;
import com.solveria.core.workforce.domain.model.vo.PositionStatus;
import com.solveria.core.workforce.infrastructure.jpa.JobJpa;
import com.solveria.core.workforce.infrastructure.jpa.PositionJpa;
import com.solveria.core.workforce.infrastructure.mapper.PositionMapper;
import com.solveria.core.workforce.infrastructure.repository.JobRepository;
import com.solveria.core.workforce.infrastructure.repository.PositionRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PositionRepositoryAdapter implements PositionRepositoryPort {

  private final PositionRepository positionRepository;
  private final PositionMapper positionMapper;
  private final EventOutboxPort eventOutboxPort;
  private final JobRepository jobRepository;

  @Override
  @Transactional
  @CacheEvict(
      value = "positions",
      key = "#result.positionId + '-' + #result.tenantId",
      condition = "#result != null")
  public Position save(Position position) {
    PositionJpa positionJpa =
        positionRepository
            .findById(position.getPositionId())
            .map(
                existing -> {
                  positionMapper.updateJpa(position, existing);
                  if (position.getJobId() != null) {
                    JobJpa jobJpa =
                        jobRepository
                            .findById(position.getJobId())
                            .orElseThrow(
                                () ->
                                    new IllegalArgumentException(
                                        "Job not found: " + position.getJobId()));
                    existing.setJob(jobJpa);
                  }
                  return existing;
                })
            .orElseGet(
                () -> {
                  PositionJpa jpa = positionMapper.toJpa(position);
                  UUID tenantId = UUID.fromString(SecurityTenantContext.getCurrentTenantId());
                  jpa.setTenantId(tenantId);
                  if (position.getJobId() != null) {
                    JobJpa jobJpa =
                        jobRepository
                            .findById(position.getJobId())
                            .orElseThrow(
                                () ->
                                    new IllegalArgumentException(
                                        "Job not found: " + position.getJobId()));
                    jpa.setJob(jobJpa);
                  }
                  return jpa;
                });

    PositionJpa savedPositionJpa = positionRepository.save(positionJpa);
    Position savedPosition = positionMapper.toDomain(savedPositionJpa);

    eventOutboxPort.publish(position.pullDomainEvents());

    return savedPosition;
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "positions", key = "#positionId + '-' + #tenantId", unless = "#result == null")
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
  @Transactional(readOnly = true)
  public int countByJobIdAndTenantId(UUID jobId, UUID tenantId) {
    UUID currentTenantId = UUID.fromString(SecurityTenantContext.getCurrentTenantId());
    if (!currentTenantId.equals(tenantId)) {
      return 0;
    }
    return positionRepository.countByJobIdAndTenantId(jobId, currentTenantId);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Position> findByTenantId(UUID tenantId, Pageable pageable) {
    UUID currentTenantId = UUID.fromString(SecurityTenantContext.getCurrentTenantId());
    if (!currentTenantId.equals(tenantId)) {
      return Page.empty(pageable);
    }
    List<Position> positions =
        positionRepository.findByTenantId(currentTenantId).stream()
            .map(positionMapper::toDomain)
            .toList();
    return PageUtils.slice(positions, pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Position> findByTenantIdAndStatus(
      UUID tenantId, PositionStatus status, Pageable pageable) {
    UUID currentTenantId = UUID.fromString(SecurityTenantContext.getCurrentTenantId());
    if (!currentTenantId.equals(tenantId)) {
      return Page.empty(pageable);
    }
    List<Position> positions =
        positionRepository.findByTenantIdAndStatus(currentTenantId, status).stream()
            .map(positionMapper::toDomain)
            .toList();
    return PageUtils.slice(positions, pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existsByUnitIdAndJobIdAndTenantId(UUID unitId, UUID jobId, UUID tenantId) {
    UUID currentTenantId = UUID.fromString(SecurityTenantContext.getCurrentTenantId());
    if (!currentTenantId.equals(tenantId)) {
      return false;
    }
    return positionRepository.existsByUnitIdAndJobIdAndTenantId(unitId, jobId, currentTenantId);
  }
}
