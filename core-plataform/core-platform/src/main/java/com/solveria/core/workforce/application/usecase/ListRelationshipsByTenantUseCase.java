package com.solveria.core.workforce.application.usecase;

import com.solveria.core.security.context.SecurityTenantContext;
import com.solveria.core.workforce.application.dto.RelationshipResponse;
import com.solveria.core.workforce.application.port.RelationshipRepositoryPort;
import com.solveria.core.workforce.domain.model.Relationship;
import com.solveria.core.workforce.infrastructure.mapper.RelationshipMapper;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListRelationshipsByTenantUseCase {

  private final RelationshipRepositoryPort relationshipRepositoryPort;
  private final RelationshipMapper relationshipMapper;

  @Transactional(readOnly = true)
  public Page<RelationshipResponse> execute(Pageable pageable) {
    UUID tenantId = UUID.fromString(SecurityTenantContext.getCurrentTenantId());
    Page<Relationship> relationships = relationshipRepositoryPort.findByTenantId(tenantId, pageable);
    return relationships.map(relationshipMapper::toResponse);
  }
}
