package com.solveria.core.workforce.application.web;

import com.solveria.core.security.context.SecurityUserContext;
import com.solveria.core.shared.pagination.PageUtils;
import com.solveria.core.workforce.application.dto.*;
import com.solveria.core.workforce.application.dto.GetPersonByDNIResponse;
import com.solveria.core.workforce.application.usecase.*;
import com.solveria.core.workforce.domain.model.vo.ContactPoint;
import com.solveria.core.workforce.domain.model.vo.MaritalStatus;
import com.solveria.core.workforce.infrastructure.jpa.PersonJpa;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/persons")
@RequiredArgsConstructor
@Tag(name = "Person", description = "Gestión de Identidad Civil Única (Master Data)")
public class PersonController {

  private final CreatePersonUseCase createPersonUseCase;
  private final UpdatePersonUseCase updatePersonUseCase;
  private final ResolveDeduplicationUseCase resolveDeduplicationUseCase;
  private final GetPersonByDNIUseCase getPersonByDNIUseCase;
  private final GetAllPersonsUseCase getAllPersonsUseCase;
  private final GetPersonMeUseCase getPersonMeUseCase;

  @GetMapping("/me")
  public ResponseEntity<PersonMeResponse> me() {
    Long userId = SecurityUserContext.getUserId();

    System.out.println("USER ID FROM TOKEN = " + userId);

    PersonMeResponse response = getPersonMeUseCase.execute(userId);
    return ResponseEntity.ok(response);
  }

  @PostMapping
  @Operation(
      summary = "Crear registro maestro de persona",
      description = "Registra una nueva identidad física en el sistema Core.")
  public ResponseEntity<PersonResponse> create(@Valid @RequestBody CreatePersonRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(createPersonUseCase.execute(request));
  }

  @PutMapping("/{personId}")
  @Operation(
      summary = "Actualizar persona",
      description = "Actualiza estado civil, profesion y contacto principal de la persona.")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Persona actualizada"),
    @ApiResponse(responseCode = "400", description = "Solicitud invalida", content = @Content),
    @ApiResponse(responseCode = "404", description = "Persona no encontrada", content = @Content),
    @ApiResponse(responseCode = "500", description = "Error interno", content = @Content)
  })
  public ResponseEntity<Void> update(
      @PathVariable UUID personId, @Valid @RequestBody UpdatePersonRequest request) {
    MaritalStatus maritalStatus = null;
    if (StringUtils.hasText(request.getMaritalStatus())) {
      maritalStatus = MaritalStatus.valueOf(request.getMaritalStatus().toUpperCase());
    }

    List<ContactPoint> contacts = null;
    if (hasContactData(request)) {
      contacts =
          List.of(
              ContactPoint.create(request.getEmail(), request.getPhone(), request.getAddress()));
    }

    updatePersonUseCase.execute(personId, maritalStatus, request.getProfessionTitle(), contacts);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{personId}/resolve-deduplication")
  @Operation(
      summary = "Resolver deduplicacion",
      description = "Marca un registro duplicado y consolida la identidad principal.")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Deduplicacion resuelta"),
    @ApiResponse(responseCode = "400", description = "Solicitud invalida", content = @Content),
    @ApiResponse(responseCode = "404", description = "Persona no encontrada", content = @Content),
    @ApiResponse(responseCode = "500", description = "Error interno", content = @Content)
  })
  public ResponseEntity<Void> resolveDeduplication(
      @PathVariable UUID personId, @Valid @RequestBody ResolveDeduplicationRequest request) {
    resolveDeduplicationUseCase.execute(personId, request.getDuplicateId());
    return ResponseEntity.noContent().build();
  }

  private boolean hasContactData(UpdatePersonRequest request) {
    return StringUtils.hasText(request.getEmail())
        || StringUtils.hasText(request.getPhone())
        || StringUtils.hasText(request.getAddress());
  }

  @GetMapping("/dni")
  @Operation(
      summary = "Obtener persona por DNI",
      description =
          "Busca un registro de persona utilizando su DNI. Retorna 404 si no se encuentra la persona.")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Persona encontrada"),
    @ApiResponse(responseCode = "400", description = "Solicitud invalida", content = @Content),
    @ApiResponse(responseCode = "404", description = "Persona no encontrada", content = @Content),
    @ApiResponse(responseCode = "500", description = "Error interno", content = @Content)
  })
  public ResponseEntity<GetPersonByDNIResponse> getByDNI(@RequestParam String dni) {

    GetPersonByDNIResponse response = getPersonByDNIUseCase.execute(dni);
    if (response == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(response);
  }

  @GetMapping
  @Operation(
      summary = "Listar personas",
      description = "Obtiene un listado paginado de personas registradas en el sistema.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Listado paginado obtenido"),
    @ApiResponse(responseCode = "400", description = "Solicitud invalida", content = @Content),
    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
    @ApiResponse(responseCode = "500", description = "Error interno", content = @Content)
  })
  public ResponseEntity<Page<PersonResponse>> list(Pageable pageable) {
    Pageable sanitized = PageUtils.sanitize(pageable, PersonJpa.class);
    return ResponseEntity.ok(getAllPersonsUseCase.execute(sanitized));
  }
}
