package com.solveria.core.legal.domain.exception;

import com.solveria.core.shared.exceptions.DomainException;
import java.util.Map;
import java.util.UUID;

public class EvidenceDataMissingException extends DomainException {

  public EvidenceDataMissingException(UUID contractId) {
    super("LEGAL_EVIDENCE_DATA_MISSING", Map.of("contractId", contractId), null);
  }
}
