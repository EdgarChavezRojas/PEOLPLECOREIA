package com.solveria.core.workforce.domain.exception;

import java.util.UUID;

public class PositionNotFoundException extends SolverException {
    public PositionNotFoundException(UUID id) { super("Plaza (Position) no encontrada con ID: " + id); }
}