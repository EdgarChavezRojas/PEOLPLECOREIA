package com.solveria.payroll.infrastructure.adapter;

import com.solveria.payroll.application.port.outbound.TenantProfilePort;
import com.solveria.payroll.domain.model.vo.TenantProfile;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class TenantProfileAdapter implements TenantProfilePort {

  @Override
  public TenantProfile resolve(UUID tenantId) {
    String str = tenantId.toString();
    if (str.endsWith("1") || str.endsWith("a")) {
      return TenantProfile.ONG;
    } else if (str.endsWith("2") || str.endsWith("b")) {
      return TenantProfile.RETAIL;
    } else if (str.endsWith("3") || str.endsWith("c")) {
      return TenantProfile.EDUCACION;
    }
    return TenantProfile.CORPORATIVO;
  }
}
