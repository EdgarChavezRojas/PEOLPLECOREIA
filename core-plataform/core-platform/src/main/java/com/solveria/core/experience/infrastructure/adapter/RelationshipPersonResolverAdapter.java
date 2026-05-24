package com.solveria.core.experience.infrastructure.adapter;

import com.solveria.core.experience.application.port.out.RelationshipPersonResolverPort;
import com.solveria.core.workforce.infrastructure.jpa.RelationshipJpa;
import com.solveria.core.workforce.infrastructure.repository.RelationshipRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Adapter: Implementación del puerto ACL {@link RelationshipPersonResolverPort}.
 *
 * <p>Resuelve el {@code personId} asociado a un {@code relationshipId} consultando directamente el
 * repositorio JPA del BC Workforce. Este adapter actúa como capa Anti-Corruption (ACL) que
 * desacopla Experience del modelo de dominio de Workforce, accediendo únicamente a la entidad de
 * infraestructura {@link RelationshipJpa}.
 *
 * <p>En un monolito modular, el acceso directo al repositorio JPA de otro BC es aceptable como
 * estrategia ACL pragmática. Si se migra a microservicios, este adapter se reemplazaría por una
 * llamada HTTP/gRPC al servicio Workforce.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RelationshipPersonResolverAdapter implements RelationshipPersonResolverPort {

  private final RelationshipRepository relationshipRepository;

  @Override
  public UUID resolvePersonIdByRelationship(UUID relationshipId) {
    if (relationshipId == null) {
      throw new IllegalArgumentException("relationshipId no puede ser nulo");
    }

    log.debug("event=EXP_RESOLVE_PERSON_BY_RELATIONSHIP_START relationshipId={}", relationshipId);

    RelationshipJpa relationship =
        relationshipRepository
            .findById(relationshipId)
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Relación laboral no encontrada: " + relationshipId));

    UUID personId = relationship.getPersonId();

    if (personId == null) {
      throw new IllegalArgumentException(
          "La relación laboral " + relationshipId + " no tiene persona asociada");
    }

    log.debug(
        "event=EXP_RESOLVE_PERSON_BY_RELATIONSHIP_OK relationshipId={} personId={}",
        relationshipId,
        personId);

    return personId;
  }
}
