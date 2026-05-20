package com.solveria.core.workforce.application.usecase;

import com.solveria.core.security.context.SecurityTenantContext;
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
public class UpdatePositionBudgetUseCase {

  private static final String POSITION_NOT_FOUND = "POSITION_NOT_FOUND";

  private final PositionRepositoryPort positionRepositoryPort;

  public void execute(UUID positionId, boolean isBudgeted) {
    UUID tenantId = UUID.fromString(SecurityTenantContext.getCurrentTenantId());
    Position position =
        positionRepositoryPort
            .findByPositionIdAndTenantId(positionId, tenantId)
            .orElseThrow(() -> new SolverException(POSITION_NOT_FOUND));

    position.updateBudgeted(isBudgeted);
    positionRepositoryPort.save(position);

    log.info("event=CORE_WORKFORCE_POSITION_BUDGET_UPDATE_SUCCESS targetId={}", positionId);
  }
}
