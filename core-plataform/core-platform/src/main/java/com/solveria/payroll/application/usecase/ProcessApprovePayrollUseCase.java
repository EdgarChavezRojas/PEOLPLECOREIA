package com.solveria.payroll.application.usecase;

import com.solveria.payroll.application.dto.request.ApprovePayrollRequest;
import com.solveria.payroll.application.port.inbound.ApprovePayrollUseCase;
import com.solveria.payroll.application.port.outbound.PayrollApprovalRepositoryPort;
import com.solveria.payroll.domain.model.entity.PayrollApproval;
import com.solveria.core.shared.exceptions.SolverException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ProcessApprovePayrollUseCase implements ApprovePayrollUseCase {

    private final PayrollApprovalRepositoryPort payrollApprovalRepositoryPort;

    public ProcessApprovePayrollUseCase(PayrollApprovalRepositoryPort payrollApprovalRepositoryPort) {
        this.payrollApprovalRepositoryPort = payrollApprovalRepositoryPort;
    }

    @Override
    @Transactional
    public void execute(UUID runId, ApprovePayrollRequest request, String tenantId) {
        PayrollApproval approval = payrollApprovalRepositoryPort.findByRunRef(runId)
                .orElseThrow(() -> new SolverException("APPROVAL_NOT_FOUND", "Payroll approval not found for runId: " + runId));
        
        approval.approve(UUID.randomUUID()); 
        
        payrollApprovalRepositoryPort.save(approval);
    }
}
