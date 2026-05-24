package com.solveria.core.accruals.domain.policy;

import com.solveria.core.accruals.domain.model.vo.SeniorityBaseType;
import java.math.BigDecimal;

public final class SeniorityBasePolicy {

  private static final BigDecimal BASE_1_SMN = BigDecimal.valueOf(3300);
  private static final BigDecimal BASE_3_SMN = BigDecimal.valueOf(9900);

  private SeniorityBasePolicy() {}

  public static SeniorityBaseType resolveBaseType(String tenantSegment) {
    if (tenantSegment == null || tenantSegment.isBlank()) {
      return SeniorityBaseType.BASE_3_SMN;
    }
    String normalized = tenantSegment.trim().toUpperCase();
    return switch (normalized) {
      case "ONG", "EDUCACION" -> SeniorityBaseType.BASE_1_SMN;
      default -> SeniorityBaseType.BASE_3_SMN;
    };
  }

  public static BigDecimal resolveBaseAmount(String tenantSegment) {
    SeniorityBaseType type = resolveBaseType(tenantSegment);
    return type == SeniorityBaseType.BASE_1_SMN ? BASE_1_SMN : BASE_3_SMN;
  }
}
