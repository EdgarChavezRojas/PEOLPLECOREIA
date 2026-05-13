package com.solveria.core.legal.application.port;

import java.time.Instant;
import java.util.UUID;

public interface DigitalKardexPort {

  String storeEvidence(UUID contractId, String tenantId, byte[] fileContent, Instant generatedAt);
}

