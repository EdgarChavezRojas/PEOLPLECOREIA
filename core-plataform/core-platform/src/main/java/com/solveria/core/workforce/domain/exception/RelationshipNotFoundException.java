package com.solveria.core.workforce.domain.exception;

import java.util.UUID;

public class RelationshipNotFoundException extends SolverException {
  public RelationshipNotFoundException(UUID id) {
    super("Relación laboral no encontrada con ID: " + id);
  }
}
