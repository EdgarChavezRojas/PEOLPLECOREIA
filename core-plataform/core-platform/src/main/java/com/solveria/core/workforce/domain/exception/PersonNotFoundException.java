package com.solveria.core.workforce.domain.exception;

import java.io.Serial;

public class PersonNotFoundException extends SolverException {
  @Serial private static final long serialVersionUID = 1L;
  private static final String PERSON_NOT_FOUND = "PERSON_NOT_FOUND";

  public PersonNotFoundException(String message) {
    super(PERSON_NOT_FOUND, message);
  }
}
