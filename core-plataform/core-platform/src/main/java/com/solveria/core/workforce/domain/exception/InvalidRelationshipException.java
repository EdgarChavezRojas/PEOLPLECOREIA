package com.solveria.core.workforce.domain.exception;

import java.io.Serial;

/**
 * Excepción: Invariante de Relación Laboral violada
 *
 * <p>Ej: No Traslape de Vínculos Primarios
 */
public class InvalidRelationshipException extends SolverException {
  @Serial private static final long serialVersionUID = 1L;
  private static final String INVALID_RELATIONSHIP = "INVALID_RELATIONSHIP";

  public InvalidRelationshipException(String message) {
    super(INVALID_RELATIONSHIP, message);
  }
}
