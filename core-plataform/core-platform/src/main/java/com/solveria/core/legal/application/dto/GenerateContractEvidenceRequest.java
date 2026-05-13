package com.solveria.core.legal.application.dto;

import java.util.UUID;

public record GenerateContractEvidenceRequest(UUID contractId, String tenantId) {}

