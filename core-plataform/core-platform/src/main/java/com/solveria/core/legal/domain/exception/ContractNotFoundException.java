package com.solveria.core.legal.domain.exception;

import com.solveria.core.shared.exceptions.DomainException;
import java.util.Map;
import java.util.UUID;

public class ContractNotFoundException extends DomainException {

  public ContractNotFoundException(UUID contractId) {
    super("LEGAL_CONTRACT_NOT_FOUND", Map.of("contractId", contractId), null);
  }
}

