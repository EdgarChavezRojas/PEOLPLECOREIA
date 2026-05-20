package com.solveria.core.workforce.infrastructure.repository;

import com.solveria.core.workforce.infrastructure.jpa.JobJpa;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobRepository extends JpaRepository<JobJpa, UUID> {

  // Búsqueda filtrando por el multi-tenant para evitar fugas de datos
  Optional<JobJpa> findByJobIdAndTenantId(UUID jobId, UUID tenantId);

  // Verificación rápida de existencia (SELECT COUNT)
  boolean existsByJobIdAndTenantId(UUID jobId, UUID tenantId);
}
