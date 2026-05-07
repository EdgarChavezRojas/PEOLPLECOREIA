package com.solveria.core.workforce.domain.exception;

import java.io.Serial;

public class PersonAlreadyExistsException extends SolverException {

  @Serial private static final long serialVersionUID = 1L;
  private static final String PERSON_EXISTS = "PERSON_ALREADY_EXISTS";

  public PersonAlreadyExistsException(String message) {
    super(PERSON_EXISTS, message);
  }
}
