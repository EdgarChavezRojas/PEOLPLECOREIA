package com.solveria.scheduling.application.web;

import com.solveria.scheduling.application.dto.response.ScheduleEmployeeResponseDto;
import com.solveria.scheduling.application.port.inbound.ScheduleQueryUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Controlador REST para consultas asociadas a planificación de horarios y turnos. */
@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
@Tag(
    name = "Schedule Queries",
    description = "Endpoints para consultas de horarios de trabajadores")
public class ScheduleQueryController {

  private final ScheduleQueryUseCase scheduleQueryUseCase;

  /**
   * Endpoint para obtener los turnos asignados de un empleado específico en un rango de fechas.
   *
   * @param relationshipId id de la relación laboral del empleado en Core HR.
   * @param startDate fecha inicial del rango de búsqueda (ISO DATE: YYYY-MM-DD).
   * @param endDate fecha final del rango de búsqueda (ISO DATE: YYYY-MM-DD).
   * @return DTO conteniendo el listado de turnos correspondientes.
   */
  @GetMapping("/employees/{relationshipId}")
  @Operation(
      summary = "Obtener el horario/turnos de un empleado en un rango de fechas",
      description =
          "Retorna una lista con todos los turnos asignados y activos de un empleado específico en un rango de fechas determinado.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Horario recuperado exitosamente",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ScheduleEmployeeResponseDto.class))),
    @ApiResponse(
        responseCode = "400",
        description = "Solicitud inválida o empleado inactivo",
        content = @Content),
    @ApiResponse(responseCode = "404", description = "Recurso no encontrado", content = @Content),
    @ApiResponse(
        responseCode = "500",
        description = "Error interno del servidor",
        content = @Content)
  })
  public ResponseEntity<ScheduleEmployeeResponseDto> getEmployeeSchedule(
      @PathVariable("relationshipId") UUID relationshipId,
      @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

    ScheduleEmployeeResponseDto response =
        scheduleQueryUseCase.getEmployeeSchedule(relationshipId, startDate, endDate);
    return ResponseEntity.ok(response);
  }
}
