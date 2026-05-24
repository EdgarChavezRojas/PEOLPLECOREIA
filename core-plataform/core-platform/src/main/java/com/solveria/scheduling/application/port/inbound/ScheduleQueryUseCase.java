package com.solveria.scheduling.application.port.inbound;

import com.solveria.scheduling.application.dto.response.ScheduleEmployeeResponseDto;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Puerto de entrada (interfaz) para el caso de uso de consulta de horarios de empleados.
 */
public interface ScheduleQueryUseCase {

  /**
   * Obtiene el horario/turnos de un empleado dentro de un rango de fechas.
   *
   * @param relationshipId el ID de la relación laboral del empleado en Core HR.
   * @param startDate fecha inicial del rango.
   * @param endDate fecha final del rango.
   * @return DTO conteniendo la lista de turnos asignados y activos.
   */
  ScheduleEmployeeResponseDto getEmployeeSchedule(
      UUID relationshipId, LocalDate startDate, LocalDate endDate);
}
