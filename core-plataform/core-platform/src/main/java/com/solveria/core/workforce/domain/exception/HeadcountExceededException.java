package com.solveria.core.workforce.domain.exception;

/**
 * Excepción: Control de Plazas - No hay slots disponibles
 *
 * <p>Invariante: Control de Plazas (Headcount): Impide asignar personal a una posición sin plaza
 * vacante autorizada presupuestariamente.
 */
public class HeadcountExceededException extends SolverException {

  private static final long serialVersionUID = 1L;
  private static final String HEADCOUNT_EXCEEDED = "HEADCOUNT_EXCEEDED";

  public HeadcountExceededException(String message) {
    super(HEADCOUNT_EXCEEDED, message);
  }
}
