package com.solveria.core.workforce.application.port;

import com.solveria.core.workforce.domain.model.Position;
import com.solveria.core.workforce.domain.model.vo.PositionStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Puerto de Salida: Repository Abstracción para Position (Plazas presupuestadas) */
public interface PositionRepositoryPort {

  /** Guarda una posición */
  Position save(Position position);

  /** Busca por ID y Tenant */
  Optional<Position> findByPositionIdAndTenantId(UUID positionId, UUID tenantId);

  /** Busca posiciones por Job ID (para validación de headcount) */
  int countByJobIdAndTenantId(UUID jobId, UUID tenantId);

  /** Lista posiciones por tenant */
  Page<Position> findByTenantId(UUID tenantId, Pageable pageable);

  /** Lista posiciones por tenant y status */
  Page<Position> findByTenantIdAndStatus(UUID tenantId, PositionStatus status, Pageable pageable);
}
