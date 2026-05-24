package com.solveria.scheduling.application.dto.response;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO contenedor de respuesta que agrupa los turnos asignados a un empleado
 * dentro de un rango de fechas específico.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleEmployeeResponseDto {

  private UUID relationshipId;
  private LocalDate startDate;
  private LocalDate endDate;
  private List<AssignedShiftResponseDto> shifts;
}
