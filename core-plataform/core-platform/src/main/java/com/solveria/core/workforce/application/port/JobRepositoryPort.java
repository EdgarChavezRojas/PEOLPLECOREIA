package com.solveria.core.workforce.application.port;

import com.solveria.core.workforce.domain.model.Job;
import java.util.Optional;
import java.util.UUID;

public interface JobRepositoryPort {

  /**
   * Busca un cargo maestro (Job Descriptor) asegurando que pertenezca al Tenant actual.
   * Indispensable para validar reglas antes de crear o modificar una Position. * @param jobId
   * Identificador del cargo (Ej. ID para "Cajero")
   *
   * @param tenantId Identificador de la empresa
   * @return El objeto de dominio Job si existe
   */
  Optional<Job> findByJobIdAndTenantId(UUID jobId, UUID tenantId);

  /** Verifica la existencia de un cargo de forma rápida. */
  boolean existsByJobIdAndTenantId(UUID jobId, UUID tenantId);
}
