package com.solveria.core.legal.application.dto;

import java.math.BigDecimal;

public record UpdateLegalThresholdRequest(String ruleName, BigDecimal newValue, String tenantId, String userId) {}

