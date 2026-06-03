package com.solveria.core.workforce.application.port;

import com.solveria.core.workforce.domain.model.Relationship;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Puerto de Salida: Repository Abstracción para Relationship (Vínculos laborales/académicos) */
public interface RelationshipRepositoryPort {

  /** Guarda una relación */
  Relationship save(Relationship relationship);

  /** Busca por ID y Tenant */
  Optional<Relationship> findByRelationshipIdAndTenantId(UUID relationshipId, UUID tenantId);

  /** Valida que no exista vínculo laboral primario para una persona en un tenant */
  boolean existsPrimaryRelationshipForPersonInTenant(UUID personId, UUID tenantId);

  List<Relationship> findByPersonId(UUID personId);

  List<Relationship> findAll();
}
