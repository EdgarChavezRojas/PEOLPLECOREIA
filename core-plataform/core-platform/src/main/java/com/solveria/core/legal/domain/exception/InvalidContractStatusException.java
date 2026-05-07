package com.solveria.core.legal.domain.exception;

import com.solveria.core.legal.domain.model.vo.ContractStatus;
import com.solveria.core.shared.exceptions.DomainException;
import java.util.Map;

public class InvalidContractStatusException extends DomainException {

  public InvalidContractStatusException(
      ContractStatus currentStatus, ContractStatus expectedStatus) {
    super(
        "INVALID_CONTRACT_STATUS",
        Map.of("currentStatus", currentStatus, "expectedStatus", expectedStatus),
        null);
  }
}
