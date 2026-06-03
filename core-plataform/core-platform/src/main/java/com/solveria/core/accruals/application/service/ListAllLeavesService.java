package com.solveria.core.accruals.application.service;

import com.solveria.core.accruals.application.port.AccrualBalanceRepositoryPort;
import com.solveria.core.accruals.application.usecase.ListAllLeavesUseCase;
import com.solveria.core.accruals.domain.model.LeaveTransaction;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListAllLeavesService implements ListAllLeavesUseCase {

  private final AccrualBalanceRepositoryPort accrualBalanceRepositoryPort;

  @Override
  public Page<LeaveTransaction> handle(Pageable pageable) {
    return accrualBalanceRepositoryPort.findAllLeaves(pageable);
  }
}
