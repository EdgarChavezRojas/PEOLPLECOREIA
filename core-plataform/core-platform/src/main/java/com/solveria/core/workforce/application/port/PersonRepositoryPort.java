package com.solveria.core.workforce.application.port;

import com.solveria.core.workforce.domain.model.Person;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de Salida: Repository Abstracción para Person
 *
 * <p>Los Use Cases inyectan SOLO esta interfaz, NO PersonRepository de Spring Data.
 */
public interface PersonRepositoryPort {

  /**
   * Guarda una persona en el dominio. El adapter internamente maneja: mapeo, persistencia, mapeo de
   * vuelta.
   *
   * @param person Entidad de dominio
   * @return Entidad de dominio guardada
   */
  Person save(Person person);

  /** Busca por GlobalID (deduplicación) */
  Optional<Person> findByGlobalId(String globalId);

  /** Verifica existencia por GlobalID */
  boolean existsByGlobalId(String globalId);

  /** Busca por ID */
  Optional<Person> findByPersonId(UUID personId);
}
