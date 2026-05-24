package com.solveria.core.workforce.application.usecase;

import com.solveria.core.workforce.application.port.PositionRepositoryPort;
import com.solveria.core.workforce.domain.exception.SolverException;
import com.solveria.core.workforce.domain.model.Position;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class VacatePositionUseCase {

  private static final String POSITION_NOT_FOUND = "POSITION_NOT_FOUND";

  private final PositionRepositoryPort positionRepositoryPort;

  public void execute(UUID positionId, UUID tenantId) {
    Position position =
        positionRepositoryPort
            .findByPositionIdAndTenantId(positionId, tenantId)
            .orElseThrow(() -> new SolverException(POSITION_NOT_FOUND));

    position.vacate();
    positionRepositoryPort.save(position);

    log.info("event=CORE_WORKFORCE_POSITION_VACATE_SUCCESS targetId={}", positionId);
  }
}
