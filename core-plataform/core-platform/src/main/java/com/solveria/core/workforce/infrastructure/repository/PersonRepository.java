package com.solveria.core.workforce.infrastructure.repository;

import com.solveria.core.workforce.infrastructure.jpa.PersonJpa;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonRepository extends JpaRepository<PersonJpa, UUID> {

  boolean existsByGlobalId(String globalId);

  Optional<PersonJpa> findByGlobalId(String globalId);

  Optional<PersonJpa> findByDNI(String dni);
}
