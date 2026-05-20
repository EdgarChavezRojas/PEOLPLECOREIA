package com.solveria.payroll.application.port.outbound;

import com.solveria.payroll.domain.model.entity.PayrollApproval;
import java.util.Optional;
import java.util.UUID;

public interface PayrollApprovalRepositoryPort {
  PayrollApproval save(PayrollApproval payrollApproval);

  Optional<PayrollApproval> findByRunRef(UUID runRef);
}
