package com.solveria.scheduling.domain.exception;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción lanzada cuando el empleado consultado no existe o no se encuentra activo en el sistema
 * de Core HR (BC 01).
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Employee is not active or does not exist")
public class EmployeeNotActiveException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public EmployeeNotActiveException(UUID relationshipId) {
    super(
        "El empleado con relación ID "
            + relationshipId
            + " no existe o no está activo en Core HR.");
  }
}
