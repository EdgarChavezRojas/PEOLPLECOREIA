package com.solveria.core.legal.application.dto;

import java.util.UUID;

public record ApproveContractRequest(UUID contractId, String tenantId) {}
