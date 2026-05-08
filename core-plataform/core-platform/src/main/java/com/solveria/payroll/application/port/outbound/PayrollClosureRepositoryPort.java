package com.solveria.payroll.application.port.outbound;

import com.solveria.payroll.domain.model.entity.PayrollClosure;

import java.util.Optional;
import java.util.UUID;

public interface PayrollClosureRepositoryPort {
    PayrollClosure save(PayrollClosure payrollClosure);
    Optional<PayrollClosure> findByRunRef(UUID runRef);
}
