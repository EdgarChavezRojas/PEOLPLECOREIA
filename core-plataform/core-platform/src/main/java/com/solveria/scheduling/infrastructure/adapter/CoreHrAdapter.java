package com.solveria.scheduling.infrastructure.adapter;

import com.solveria.core.security.context.SecurityTenantContext;
import com.solveria.core.workforce.application.port.RelationshipRepositoryPort;
import com.solveria.core.workforce.domain.model.Relationship;
import com.solveria.core.workforce.domain.model.vo.RelationshipStatus;
import com.solveria.scheduling.application.port.outbound.CoreHrPort;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Adaptador de infraestructura que implementa CoreHrPort para conectarse con el módulo Core HR (BC
 * 01) de manera directa en el monolito, consultando al puerto del repositorio de relaciones
 * laborales.
 */
@Component
@RequiredArgsConstructor
public class CoreHrAdapter implements CoreHrPort {

  private final RelationshipRepositoryPort relationshipRepositoryPort;

  @Override
  public boolean isEmployeeActive(UUID relationshipId) {
    // 1. Obtener tenantId de la sesión de seguridad
    UUID tenantId = UUID.fromString(SecurityTenantContext.getCurrentTenantId());

    // 2. Buscar la relación laboral a través del puerto del repositorio de workforce
    Optional<Relationship> relationshipOpt =
        relationshipRepositoryPort.findByRelationshipIdAndTenantId(relationshipId, tenantId);

    // 3. Validar si la relación laboral existe y tiene el estado activo
    return relationshipOpt
        .map(relationship -> RelationshipStatus.ACTIVE.equals(relationship.getCurrentStatus()))
        .orElse(false);
  }
}
