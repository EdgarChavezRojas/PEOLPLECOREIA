package com.solveria.scheduling.domain.exception;

/** Excepción lanzada cuando se viola una regla invariante del dominio. */
public class DomainRuleViolationException extends RuntimeException {

  public DomainRuleViolationException(String message) {
    super(message);
  }
}
