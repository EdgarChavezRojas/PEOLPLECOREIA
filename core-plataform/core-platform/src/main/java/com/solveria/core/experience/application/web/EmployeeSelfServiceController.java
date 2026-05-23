package com.solveria.core.experience.application.web;

import com.solveria.core.experience.application.command.RequestLeaveCommand;
import com.solveria.core.experience.application.dto.LeaveRequestWebDto;
import com.solveria.core.experience.application.usecase.EmployeeSelfServiceUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/self-service")
@RequiredArgsConstructor
@Tag(name = "ESS - Self Service", description = "Operaciones de autoservicio del empleado")
public class EmployeeSelfServiceController {

  private final EmployeeSelfServiceUseCase employeeSelfServiceUseCase;

  @PostMapping("/leaves")
  @Operation(
      summary = "Solicitar licencia",
      description = "Inicia una solicitud de licencia/ausencia desde ESS")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Solicitud registrada"),
    @ApiResponse(responseCode = "400", description = "Solicitud invalida", content = @Content),
    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
    @ApiResponse(responseCode = "500", description = "Error interno", content = @Content)
  })
  public ResponseEntity<UUID> requestLeave(@Valid @RequestBody LeaveRequestWebDto request) {
    RequestLeaveCommand command =
        new RequestLeaveCommand(
            request.personId(), request.leaveType(), request.startDate(), request.endDate());

    UUID actionId = employeeSelfServiceUseCase.requestLeave(command);

    return ResponseEntity.status(HttpStatus.CREATED).body(actionId);
  }
}

