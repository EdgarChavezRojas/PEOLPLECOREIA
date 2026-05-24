package com.solveria.core.workforce.domain.exception;

import java.io.Serial;
import lombok.Getter;

/**
 * Excepción base del dominio
 *
 * <p>Todas las excepciones del core heredan de esta clase. Permite manejo centralizado y coherente
 * de errores.
 */
@Getter
public class SolverException extends RuntimeException {
  @Serial private static final long serialVersionUID = 1L;
  private final String errorCode;

  public SolverException(String errorCode) {
    super(errorCode);
    this.errorCode = errorCode;
  }

  public SolverException(String errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }

  public SolverException(String errorCode, String message, Throwable cause) {
    super(message, cause);
    this.errorCode = errorCode;
  }
}
