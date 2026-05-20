package com.solveria.core.financial.domain.model.vo;

/**
 * Excepción de dominio: El proveedor de cotización UFV (Banco Central de Bolivia) no está
 * disponible o no respondió dentro del timeout establecido (5 segundos).
 *
 * <p>Clase pura sin anotaciones de infraestructura.
 */
public class UfvProviderUnavailableException extends RuntimeException {

  public UfvProviderUnavailableException(String message) {
    super(message);
  }

  public UfvProviderUnavailableException(String message, Throwable cause) {
    super(message, cause);
  }
}
