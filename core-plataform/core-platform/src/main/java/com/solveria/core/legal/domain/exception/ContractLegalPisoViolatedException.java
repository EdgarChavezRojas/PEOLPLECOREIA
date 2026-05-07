package com.solveria.core.legal.domain.exception;

import com.solveria.core.shared.exceptions.DomainException;
import java.math.BigDecimal;
import java.util.Map;

public class ContractLegalPisoViolatedException extends DomainException {

  public ContractLegalPisoViolatedException(BigDecimal basicSalary, BigDecimal floor) {
    super("CONTRACT_LEGAL_PISO_VIOLATED", Map.of("basicSalary", basicSalary, "floor", floor), null);
  }
}
