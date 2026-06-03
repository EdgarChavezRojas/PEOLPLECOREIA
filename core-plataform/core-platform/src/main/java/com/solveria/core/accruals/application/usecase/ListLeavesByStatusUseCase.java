package com.solveria.core.accruals.application.usecase;

import com.solveria.core.accruals.domain.model.LeaveTransaction;
import com.solveria.core.accruals.domain.model.vo.LeaveStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Caso de uso: Listar las licencias del tenant actual filtradas por estado (PENDING, APPROVED,
 * REJECTED).
 */
public interface ListLeavesByStatusUseCase {

  Page<LeaveTransaction> handle(LeaveStatus status, Pageable pageable);
}
