package com.solveria.core.experience.application.port.out;

import java.util.UUID;

/**
 * Secondary Port (Outbound): Puerto ACL para resolver el personId a partir de un relationshipId.
 *
 * <p>Este puerto abstrae la consulta al BC de Workforce, evitando un acoplamiento directo entre
 * Experience y el modelo de dominio de Workforce. La implementación concreta (adapter) se provee en
 * la capa de infraestructura.
 */
public interface RelationshipPersonResolverPort {

  /**
   * Resuelve el UUID de la persona asociada a la relación laboral indicada.
   *
   * @param relationshipId ID de la relación laboral (Workforce BC)
   * @return UUID del personId asociado
   * @throws IllegalArgumentException si la relación no existe o no tiene persona asociada
   */
  UUID resolvePersonIdByRelationship(UUID relationshipId);
}
