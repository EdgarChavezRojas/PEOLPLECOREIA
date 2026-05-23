package com.solveria.core.accruals.application.usecase;

import com.solveria.core.accruals.domain.model.LeaveTransaction;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ListEmployeeLeavesUseCase {

  Page<LeaveTransaction> handle(UUID personId, Pageable pageable);
}

