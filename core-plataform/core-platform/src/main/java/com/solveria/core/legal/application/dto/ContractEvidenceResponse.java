package com.solveria.core.legal.application.dto;

import java.time.Instant;
import java.util.UUID;

public record ContractEvidenceResponse(UUID contractId, String hash, Instant generatedAt) {}

