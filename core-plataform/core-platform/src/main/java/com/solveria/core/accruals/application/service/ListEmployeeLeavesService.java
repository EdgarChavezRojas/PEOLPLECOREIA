package com.solveria.core.accruals.application.service;

import com.solveria.core.accruals.application.port.AccrualBalanceRepositoryPort;
import com.solveria.core.accruals.application.usecase.ListEmployeeLeavesUseCase;
import com.solveria.core.accruals.domain.model.LeaveTransaction;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListEmployeeLeavesService implements ListEmployeeLeavesUseCase {

  private final AccrualBalanceRepositoryPort accrualBalanceRepositoryPort;

  @Override
  public Page<LeaveTransaction> handle(UUID personId, Pageable pageable) {
    return accrualBalanceRepositoryPort.findLeaveTransactionsByRelationshipId(personId, pageable);
  }
}
