package com.solveria.core.accruals.application.port;

import com.solveria.core.accruals.domain.model.AccrualBalance;
import java.util.Optional;
import java.util.UUID;

public interface AccrualBalanceRepositoryPort {

  AccrualBalance save(AccrualBalance balance);

  Optional<AccrualBalance> findById(UUID balanceId);
}
