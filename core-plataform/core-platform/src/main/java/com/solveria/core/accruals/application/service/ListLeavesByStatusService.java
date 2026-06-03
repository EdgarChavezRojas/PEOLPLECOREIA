package com.solveria.core.accruals.application.service;

import com.solveria.core.accruals.application.port.AccrualBalanceRepositoryPort;
import com.solveria.core.accruals.application.usecase.ListLeavesByStatusUseCase;
import com.solveria.core.accruals.domain.model.LeaveTransaction;
import com.solveria.core.accruals.domain.model.vo.LeaveStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListLeavesByStatusService implements ListLeavesByStatusUseCase {

  private final AccrualBalanceRepositoryPort accrualBalanceRepositoryPort;

  @Override
  public Page<LeaveTransaction> handle(LeaveStatus status, Pageable pageable) {
    return accrualBalanceRepositoryPort.findLeavesByStatus(status, pageable);
  }
}
