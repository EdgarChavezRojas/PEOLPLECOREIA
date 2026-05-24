package com.solveria.scheduling.application.port.outbound;

import java.util.UUID;

/**
 * Puerto de salida (interfaz) para validar el estado del empleado en el módulo Core HR (BC 01).
 */
public interface CoreHrPort {

  /**
   * Valida si un empleado existe y está activo.
   *
   * @param relationshipId id de la relación laboral.
   * @return true si el empleado está activo; false de lo contrario.
   */
  boolean isEmployeeActive(UUID relationshipId);
}
