package com.solveria.core.legal.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateLegalThresholdRequest(
    String ruleName, BigDecimal newValue, UUID tenantId, String userId) {}
