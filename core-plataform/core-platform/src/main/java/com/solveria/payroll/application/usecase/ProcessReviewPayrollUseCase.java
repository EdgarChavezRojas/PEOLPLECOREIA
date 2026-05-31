package com.solveria.payroll.application.usecase;

import com.solveria.core.shared.exceptions.SolverExceptionImpl;
import com.solveria.payroll.application.dto.request.ReviewPayrollRequest;
import com.solveria.payroll.application.port.inbound.ReviewPayrollUseCase;
import com.solveria.payroll.application.port.outbound.PayrollApprovalRepositoryPort;
import com.solveria.payroll.application.port.outbound.PayrollRunRepositoryPort;
import com.solveria.payroll.domain.model.entity.PayrollApproval;
import com.solveria.payroll.domain.model.vo.ApprovalStatus;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProcessReviewPayrollUseCase implements ReviewPayrollUseCase {

  private final PayrollApprovalRepositoryPort payrollApprovalRepositoryPort;
  private final PayrollRunRepositoryPort payrollRunRepositoryPort;

  public ProcessReviewPayrollUseCase(
      PayrollApprovalRepositoryPort payrollApprovalRepositoryPort,
      PayrollRunRepositoryPort payrollRunRepositoryPort) {
    this.payrollApprovalRepositoryPort = payrollApprovalRepositoryPort;
    this.payrollRunRepositoryPort = payrollRunRepositoryPort;
  }

  @Override
  @Transactional
  public void execute(UUID runId, ReviewPayrollRequest request, UUID tenantId) {
    payrollRunRepositoryPort
        .findById(runId)
        .orElseThrow(() -> new SolverExceptionImpl("PAYROLL_RUN_NOT_FOUND"));

    PayrollApproval approval =
        payrollApprovalRepositoryPort
            .findByRunRef(runId)
            .orElseGet(
                () ->
                    new PayrollApproval(
                        null,
                        runId,
                        ApprovalStatus.PENDIENTE_REVISION,
                        null,
                        null,
                        false,
                        tenantId));

    approval.review(request.reviewerId());
    payrollApprovalRepositoryPort.save(approval);
  }
}

