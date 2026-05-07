package com.solveria.core.workforce.domain.exception;

import java.io.Serial;

public class JobNotFoundException extends SolverException {
  @Serial private static final long serialVersionUID = 1L;
  private static final String JOB_NOT_FOUND = "JOB_NOT_FOUND";

  public JobNotFoundException(String message) {
    super(JOB_NOT_FOUND, message);
  }
}
