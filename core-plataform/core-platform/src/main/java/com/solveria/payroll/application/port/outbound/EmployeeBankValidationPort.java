package com.solveria.payroll.application.port.outbound;

import java.util.UUID;

public interface EmployeeBankValidationPort {
  boolean allEmployeesHaveBankAccount(UUID runRef, UUID tenantId);
}
