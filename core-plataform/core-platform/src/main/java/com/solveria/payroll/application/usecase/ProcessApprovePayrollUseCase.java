package com.solveria.payroll.application.usecase;

import com.solveria.core.shared.exceptions.SolverExceptionImpl;
import com.solveria.payroll.application.dto.request.ApprovePayrollRequest;
import com.solveria.payroll.application.port.inbound.ApprovePayrollUseCase;
import com.solveria.payroll.application.port.outbound.PayrollApprovalRepositoryPort;
import com.solveria.payroll.domain.model.entity.PayrollApproval;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProcessApprovePayrollUseCase implements ApprovePayrollUseCase {

  private final PayrollApprovalRepositoryPort payrollApprovalRepositoryPort;

  public ProcessApprovePayrollUseCase(PayrollApprovalRepositoryPort payrollApprovalRepositoryPort) {
    this.payrollApprovalRepositoryPort = payrollApprovalRepositoryPort;
  }

  @Override
  @Transactional
  public void execute(UUID runId, ApprovePayrollRequest request, UUID tenantId) {
    PayrollApproval approval =
        payrollApprovalRepositoryPort
            .findByRunRef(runId)
            .orElseThrow(() -> new SolverExceptionImpl("APPROVAL_NOT_FOUND"));

    approval.approve(UUID.randomUUID());

    payrollApprovalRepositoryPort.save(approval);
  }
}
