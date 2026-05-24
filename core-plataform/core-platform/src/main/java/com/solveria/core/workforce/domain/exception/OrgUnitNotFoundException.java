package com.solveria.core.workforce.domain.exception;

import java.io.Serial;

public class OrgUnitNotFoundException extends SolverException {
  @Serial private static final long serialVersionUID = 1L;
  private static final String ORG_UNIT_NOT_FOUND = "ORG_UNIT_NOT_FOUND";

  public OrgUnitNotFoundException(String message) {
    super(ORG_UNIT_NOT_FOUND, message);
  }
}
