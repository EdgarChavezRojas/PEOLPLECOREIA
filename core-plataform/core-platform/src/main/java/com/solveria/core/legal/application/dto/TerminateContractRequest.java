package com.solveria.core.legal.application.dto;

import java.util.UUID;

public record TerminateContractRequest(UUID contractId, UUID tenantId, String reason) {}
