package com.solveria.payroll.application.port.outbound;

import com.solveria.payroll.domain.model.vo.TenantProfile;
import java.util.UUID;

public interface TenantProfilePort {
  TenantProfile resolve(UUID tenantId);
}
