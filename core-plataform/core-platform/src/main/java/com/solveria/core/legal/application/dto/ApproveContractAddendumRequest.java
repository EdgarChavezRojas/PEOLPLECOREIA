package com.solveria.core.legal.application.dto;

import java.util.UUID;

public record ApproveContractAddendumRequest(UUID contractId, UUID addendumId, UUID tenantId) {}
