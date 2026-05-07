package com.solveria.core.workforce.infrastructure.adapter;

import com.solveria.core.shared.events.DomainEvent;
import com.solveria.core.security.context.SecurityTenantContext;
import com.solveria.core.workforce.application.port.EventOutboxPort;
import com.solveria.core.workforce.application.port.RelationshipRepositoryPort;
import com.solveria.core.workforce.domain.model.Relationship;
import com.solveria.core.workforce.domain.model.vo.RelationshipStatus;
import com.solveria.core.workforce.domain.model.vo.RelationshipType;
import com.solveria.core.workforce.infrastructure.jpa.RelationshipJpa;
import com.solveria.core.workforce.infrastructure.mapper.RelationshipMapper;
import com.solveria.core.workforce.infrastructure.repository.RelationshipRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RelationshipRepositoryAdapter implements RelationshipRepositoryPort {

  private final RelationshipRepository relationshipRepository;
  private final RelationshipMapper relationshipMapper;
  private final EventOutboxPort eventOutboxPort;

  @Override
  @Transactional
  public Relationship save(Relationship relationship) {
    RelationshipJpa relationshipJpa = relationshipMapper.toJpa(relationship);
    RelationshipJpa savedRelationshipJpa = relationshipRepository.save(relationshipJpa);
    Relationship savedRelationship = relationshipMapper.toDomain(savedRelationshipJpa);

    for (DomainEvent event : relationship.pullDomainEvents()) {
      eventOutboxPort.publish(event);
    }

    return savedRelationship;
  }

  @Override
  public Optional<Relationship> findByRelationshipIdAndTenantId(
      UUID relationshipId, UUID tenantId) {
    UUID currentTenantId = UUID.fromString(SecurityTenantContext.getCurrentTenantId());
    if (!currentTenantId.equals(tenantId)) {
      return Optional.empty();
    }
    return relationshipRepository
        .findByRelationshipIdAndTenantId(relationshipId, currentTenantId)
        .map(relationshipMapper::toDomain);
  }

  @Override
  public boolean existsPrimaryRelationshipForPersonInTenant(UUID personId, UUID tenantId) {
    UUID currentTenantId = UUID.fromString(SecurityTenantContext.getCurrentTenantId());
    if (!currentTenantId.equals(tenantId)) {
      return false;
    }
    return relationshipRepository.existsByPersonIdAndTenantIdAndRelationTypeAndCurrentStatus(
        personId, currentTenantId, RelationshipType.LABOR, RelationshipStatus.ACTIVE);
  }
}
