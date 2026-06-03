package com.solveria.payroll.application.usecase;

import com.solveria.core.security.context.SecurityUserContext;
import com.solveria.core.shared.exceptions.SolverExceptionImpl;
import com.solveria.payroll.application.port.inbound.ApprovePayrollUseCase;
import com.solveria.payroll.application.port.outbound.PayrollApprovalRepositoryPort;
import com.solveria.payroll.application.port.outbound.PayrollRunRepositoryPort;
import com.solveria.payroll.domain.model.entity.PayrollApproval;
import com.solveria.payroll.domain.model.vo.ApprovalStatus;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProcessApprovePayrollUseCase implements ApprovePayrollUseCase {

  private final PayrollApprovalRepositoryPort payrollApprovalRepositoryPort;

  public ProcessApprovePayrollUseCase(
      PayrollApprovalRepositoryPort payrollApprovalRepositoryPort,
      PayrollRunRepositoryPort payrollRunRepositoryPort) {
    this.payrollApprovalRepositoryPort = payrollApprovalRepositoryPort;
  }

  @Override
  @Transactional
  public void execute(UUID approvalId, UUID tenantId) {
    Long approverId = SecurityUserContext.getUserId();
    if (approverId == null) {
      throw new SolverExceptionImpl("APPROVER_REQUIRED");
    }

    PayrollApproval approval =
        payrollApprovalRepositoryPort
            .findByApprovalId(approvalId)
            .orElseThrow(() -> new SolverExceptionImpl("PAYROLL_APPROVAL_NOT_FOUND"));

    if (approval.getStatus() == ApprovalStatus.PENDIENTE_REVISION) {
      throw new SolverExceptionImpl("PAYROLL_APPROVAL_NOT_REVIEWED");
    }

    approval.approve(approverId);
    payrollApprovalRepositoryPort.save(approval);
  }
}
