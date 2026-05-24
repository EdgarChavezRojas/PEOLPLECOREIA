package com.solveria.core.legal.application.port;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface AuditLogPort {

  void registerEvidenceGenerated(UUID contractId, Instant generatedAt, String hash);

  void registerLegalThresholdUpdate(
      UUID policyRuleId,
      String ruleName,
      BigDecimal previousValue,
      BigDecimal newValue,
      String userId,
      Instant occurredAt);
}
