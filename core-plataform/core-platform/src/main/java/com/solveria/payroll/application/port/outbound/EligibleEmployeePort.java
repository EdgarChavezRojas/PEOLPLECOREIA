package com.solveria.payroll.application.port.outbound;


import com.solveria.payroll.application.dto.request.EligibleEmployee;

import java.util.List;
import java.util.UUID;

public interface EligibleEmployeePort {
  List<EligibleEmployee> findEligibleByTenantId(UUID tenantId);

  EligibleEmployee findById(UUID employeeId, UUID tenantId);
}
