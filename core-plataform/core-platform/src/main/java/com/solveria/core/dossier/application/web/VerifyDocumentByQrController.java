package com.solveria.core.dossier.application.web;

import com.solveria.core.dossier.application.dto.VerifyDocumentByQrRequest;
import com.solveria.core.dossier.application.usecase.VerifyDocumentByQrUseCase;
import com.solveria.core.dossier.domain.model.DocumentRecord;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST (Driving / Input Adapter) para la validación automatizada de títulos
 * universitarios y de postgrado mediante códigos QR oficiales bolivianos.
 *
 * <p>Este adaptador primario recibe la solicitud HTTP, la traduce al comando de aplicación
 * correspondiente e invoca el caso de uso ({@link VerifyDocumentByQrUseCase}). No contiene ninguna
 * lógica de negocio.
 *
 * <p><b>Principio de Responsabilidad Única:</b> La única razón de cambio de esta clase es un cambio
 * en la capa de presentación (HTTP, formato de request/response, rutas).
 *
 * <h3>Flujo de la solicitud:</h3>
 *
 * <pre>
 *  HTTP POST /api/v1/dossier/documents/verify-qr
 *       │
 *       ▼ (mapea DTO → Command)
 *  VerifyDocumentByQrCommand
 *       │
 *       ▼ (invoca puerto primario)
 *  VerifyDocumentByQrUseCase.handle(command)
 *       │
 *       ▼ (retorna)
 *  DocumentRecord → ResponseEntity&lt;DocumentRecord&gt; HTTP 200 OK
 * </pre>
 *
 * pre
 */
@RestController
@RequestMapping("/api/v1/dossier/documents/verify-qr")
@Tag(
    name = "Document Compliance QR",
    description =
        "Endpoint para validación automatizada de títulos mediante códigos QR oficiales de Bolivia")
public class VerifyDocumentByQrController {

  private final VerifyDocumentByQrUseCase verifyDocumentByQrUseCase;

  /**
   * Inyección por constructor — patrón recomendado en Arquitectura Hexagonal.
   *
   * @param verifyDocumentByQrUseCase Puerto primario (inbound port) del caso de uso.
   */
  public VerifyDocumentByQrController(VerifyDocumentByQrUseCase verifyDocumentByQrUseCase) {
    this.verifyDocumentByQrUseCase = verifyDocumentByQrUseCase;
  }

  /**
   * Verifica un título académico universitario o de postgrado leyendo el contenido del portal
   * oficial al que apunta el código QR, cruza la identidad civil del empleado y, si coincide
   * plenamente, aprueba automáticamente el documento en el Digital Kardex.
   *
   * <p>El proceso es completamente automatizado e independiente: no requiere intervención manual de
   * un revisor de RRHH para la aprobación cuando la identidad es confirmada.
   *
   * @param request DTO de entrada con los datos necesarios para iniciar la validación QR.
   * @return {@link ResponseEntity} con el {@link DocumentRecord} aprobado (HTTP 200 OK).
   */
  @PostMapping
  @Operation(
      summary = "Verificar y aprobar título profesional vía QR",
      description =
          "Inicia el flujo automatizado de validación de un título universitario o de postgrado "
              + "mediante la lectura del portal oficial boliviano referenciado por el código QR. "
              + "El sistema extrae los datos del portal (.gob.bo o .edu.bo), cruza la identidad "
              + "civil del empleado y, si coincide plenamente, aprueba automáticamente el "
              + "documento en el Digital Kardex de PeopleCoreIA con estado APPROVED. "
              + "La operación está restringida al territorio de Santa Cruz, Bolivia, y solo "
              + "acepta dominios gubernamentales y educativos bolivianos reconocidos como seguros.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description =
            "Título verificado y aprobado exitosamente en el Digital Kardex. "
                + "El DocumentRecord retornado refleja el estado APPROVED con la fecha "
                + "y datos del proceso de aprobación automática.",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DocumentRecord.class))),
    @ApiResponse(
        responseCode = "400",
        description =
            "Error de validación. Posibles causas: "
                + "(1) La URL del QR no pertenece a un dominio .gob.bo o .edu.bo seguro; "
                + "(2) La localización no corresponde a Santa Cruz, Bolivia; "
                + "(3) El CI extraído del portal no existe en el sistema; "
                + "(4) El nombre completo del portal no coincide con el registrado en PeopleCoreIA; "
                + "(5) Campos requeridos nulos o en blanco.",
        content = @Content(mediaType = "application/json")),
    @ApiResponse(
        responseCode = "500",
        description =
            "Error interno del servidor. Posibles causas: "
                + "El portal oficial no es accesible en este momento (timeout de red), "
                + "el HTML del portal no puede ser parseado correctamente, "
                + "o error inesperado en la capa de infraestructura.",
        content = @Content(mediaType = "application/json"))
  })
  public ResponseEntity<DocumentRecord> verifyByQr(
      @Valid @RequestBody VerifyDocumentByQrRequest request) {

    DocumentRecord approvedRecord = verifyDocumentByQrUseCase.handle(request.toCommand());
    return ResponseEntity.ok(approvedRecord);
  }
}
