package com.solveria.core.accruals.application.port;

import com.solveria.core.accruals.domain.model.AccrualBalance;
import com.solveria.core.accruals.domain.model.LeaveTransaction;
import com.solveria.core.accruals.domain.model.vo.LeaveStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AccrualBalanceRepositoryPort {

  AccrualBalance save(AccrualBalance balance);

  Optional<AccrualBalance> findById(UUID balanceId);

  List<AccrualBalance> findAll();

  List<AccrualBalance> findAllByRelationshipId(UUID relationshipId);

  Page<LeaveTransaction> findLeaveTransactionsByRelationshipId(
      UUID relationshipId, Pageable pageable);

  Page<LeaveTransaction> findAllLeaves(Pageable pageable);

  Page<LeaveTransaction> findLeavesByStatus(LeaveStatus status, Pageable pageable);
}
