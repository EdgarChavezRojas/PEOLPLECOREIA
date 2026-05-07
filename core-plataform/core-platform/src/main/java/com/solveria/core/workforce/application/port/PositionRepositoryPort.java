package com.solveria.core.workforce.application.port;

import com.solveria.core.workforce.domain.model.Position;
import java.util.Optional;
import java.util.UUID;

/** Puerto de Salida: Repository Abstracción para Position (Plazas presupuestadas) */
public interface PositionRepositoryPort {

  /** Guarda una posición */
  Position save(Position position);

  /** Busca por ID y Tenant */
  Optional<Position> findByPositionIdAndTenantId(UUID positionId, UUID tenantId);

  /** Busca posiciones por Job ID (para validación de headcount) */
  int countByJobIdAndTenantId(UUID jobId, UUID tenantId);
}
