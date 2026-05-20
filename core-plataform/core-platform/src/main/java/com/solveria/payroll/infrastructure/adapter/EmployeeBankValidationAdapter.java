package com.solveria.payroll.infrastructure.adapter;

import com.solveria.payroll.application.port.outbound.EmployeeBankValidationPort;
import com.solveria.payroll.infrastructure.client.CoreHrEmployeeClient;
import com.solveria.payroll.infrastructure.jpa.PayrollLineJpa;
import com.solveria.payroll.infrastructure.jpa.PayrollRunJpa;
import com.solveria.payroll.infrastructure.repository.PayrollRunSpringRepository;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class EmployeeBankValidationAdapter implements EmployeeBankValidationPort {

  private final PayrollRunSpringRepository payrollRunSpringRepository;
  private final CoreHrEmployeeClient coreHrEmployeeClient;

  public EmployeeBankValidationAdapter(
      PayrollRunSpringRepository payrollRunSpringRepository,
      CoreHrEmployeeClient coreHrEmployeeClient) {
    this.payrollRunSpringRepository = payrollRunSpringRepository;
    this.coreHrEmployeeClient = coreHrEmployeeClient;
  }

  @Override
  public boolean allEmployeesHaveBankAccount(UUID runRef, UUID tenantId) {
    PayrollRunJpa run = payrollRunSpringRepository.findById(runRef).orElse(null);
    if (run == null || run.getLines() == null) {
      return false;
    }

    for (PayrollLineJpa line : run.getLines()) {
      if (!coreHrEmployeeClient.hasSyncedBankAccount(line.getEmployeeId(), tenantId)) {
        return false;
      }
    }
    return true;
  }
}
