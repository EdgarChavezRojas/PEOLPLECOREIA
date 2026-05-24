package com.solveria.core.workforce.domain.exception;

import java.util.UUID;

/**
 * Excepción: Control de Plazas - No hay slots disponibles
 *
 * <p>Invariante: Control de Plazas (Headcount): Impide asignar personal a una posición sin plaza
 * vacante autorizada presupuestariamente.
 */
public class HeadcountExceededException extends SolverException {

  public HeadcountExceededException(UUID jobId, long maxPositions) {
    super(
        String.format(
            "No se pueden crear más plazas. El cargo con ID %s ha alcanzado su límite máximo de %d plazas (Headcount).",
            jobId, maxPositions));
  }
}
