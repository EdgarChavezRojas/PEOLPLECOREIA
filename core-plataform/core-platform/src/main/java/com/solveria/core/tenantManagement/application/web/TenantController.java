package com.solveria.core.tenantManagement.application.web;

import com.solveria.core.tenantManagement.application.dto.TenantResponse;
import com.solveria.core.tenantManagement.application.usecase.GetAllTenantsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para la gestión de Tenants.
 *
 * <p>Expone endpoints REST que utilizan los Use Cases de la capa de aplicación.
 *
 * <p>Sigue el patrón de inyección de dependencias por constructor (sin @Autowired en atributos).
 */
@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
@Tag(name = "Tenant Management", description = "Endpoints para la gestión de tenants en el sistema")
public class TenantController {

  private final GetAllTenantsUseCase getAllTenantsUseCase;

  /**
   * Obtiene la lista de todos los tenants del sistema.
   *
   * @return ResponseEntity con lista de TenantResponse (HTTP 200 OK)
   */
  @GetMapping
  @Operation(
      summary = "Obtener todos los tenants",
      description = "Retorna una lista de todos los tenants registrados en el sistema")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Lista de tenants obtenida exitosamente",
        content = @Content(schema = @Schema(implementation = TenantResponse.class)))
  })
  public ResponseEntity<List<TenantResponse>> getAllTenants() {
    List<TenantResponse> tenants = getAllTenantsUseCase.execute();
    return ResponseEntity.status(HttpStatus.OK).body(tenants);
  }
}
