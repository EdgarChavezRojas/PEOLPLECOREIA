package com.solveria.core.workforce.application.port;

import com.solveria.core.workforce.domain.model.Relationship;
import com.solveria.core.workforce.domain.model.vo.RelationshipType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

  Page<Relationship> findByTenantId(UUID tenantId, Pageable pageable);

  /**
   * Verifica si ya existe una relación ACTIVA o DRAFT del mismo tipo para la persona en el tenant
   */
  boolean existsActiveRelationshipForPersonAndType(
      UUID personId, RelationshipType relationType, UUID tenantId);
}
