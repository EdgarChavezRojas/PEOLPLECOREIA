package com.solveria.core.dossier.application.dto;

import com.solveria.core.dossier.application.command.VerifyDocumentByQrCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * DTO de entrada (Request Body) para el endpoint de verificación de títulos académicos mediante
 * código QR oficial boliviano.
 *
 * <p>Los campos {@code docId} y {@code relationshipId} referencian los registros existentes en el
 * Digital Kardex y en la relación laboral del empleado respectivamente. La URL del QR debe
 * pertenecer a un dominio {@code .gob.bo} o {@code .edu.bo}.
 *
 * <p>Se utiliza clase convencional (no Record) para mantener compatibilidad con la deserialización
 * Jackson estándar usando el constructor {@code @Default} de Lombok, siguiendo el patrón existente
 * en los controladores del módulo workforce.
 */
@Schema(
    description =
        "Cuerpo de la solicitud para la verificación automatizada de un título académico "
            + "mediante el escaneo de su código QR oficial boliviano.")
public class VerifyDocumentByQrRequest {

  @NotNull(message = "docId es obligatorio")
  @Schema(
      description =
          "Identificador UUID del registro existente en el Digital Kardex que será aprobado.",
      example = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private UUID docId;

  @NotNull(message = "relationshipId es obligatorio")
  @Schema(
      description = "Identificador UUID del vínculo laboral (EmploymentRelationship) del empleado.",
      example = "7b1c9d4a-8e3f-4a2b-b1d0-123456789abc",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private UUID relationshipId;

  @NotBlank(message = "qrUrl es obligatorio")
  @Schema(
      description =
          "URL completa del portal oficial al que apunta el código QR del título académico. "
              + "Debe pertenecer a un dominio .gob.bo o .edu.bo.",
      example = "https://verificacion.minedu.gob.bo/titulos?codigo=ABC123",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String qrUrl;

  @NotBlank(message = "location es obligatorio")
  @Schema(
      description = "Ubicación geográfica del proceso. Debe ser exactamente 'Santa Cruz, Bolivia'.",
      example = "Santa Cruz, Bolivia",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String location;

  @NotNull(message = "tenantId es obligatorio")
  @Schema(
      description = "Identificador UUID del tenant (organización) en la plataforma PeopleCoreIA.",
      example = "d1e2f3a4-b5c6-7890-abcd-ef1234567890",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private UUID tenantId;

  @NotBlank(message = "tenantSegment es obligatorio")
  @Schema(
      description = "Segmento del tenant. Valores aceptados: EDUCACION, RETAIL, ONG, CORPORATIVO.",
      example = "EDUCACION",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String tenantSegment;

  // ── Constructors ─────────────────────────────────────────────────────────────────────────

  public VerifyDocumentByQrRequest() {}

  public VerifyDocumentByQrRequest(
      UUID docId,
      UUID relationshipId,
      String qrUrl,
      String location,
      UUID tenantId,
      String tenantSegment) {
    this.docId = docId;
    this.relationshipId = relationshipId;
    this.qrUrl = qrUrl;
    this.location = location;
    this.tenantId = tenantId;
    this.tenantSegment = tenantSegment;
  }

  // ── Getters & Setters ─────────────────────────────────────────────────────────────────────

  public UUID getDocId() {
    return docId;
  }

  public void setDocId(UUID docId) {
    this.docId = docId;
  }

  public UUID getRelationshipId() {
    return relationshipId;
  }

  public void setRelationshipId(UUID relationshipId) {
    this.relationshipId = relationshipId;
  }

  public String getQrUrl() {
    return qrUrl;
  }

  public void setQrUrl(String qrUrl) {
    this.qrUrl = qrUrl;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public UUID getTenantId() {
    return tenantId;
  }

  public void setTenantId(UUID tenantId) {
    this.tenantId = tenantId;
  }

  public String getTenantSegment() {
    return tenantSegment;
  }

  public void setTenantSegment(String tenantSegment) {
    this.tenantSegment = tenantSegment;
  }

  /**
   * Proyecta este DTO de entrada en el comando inmutable de la capa de aplicación.
   *
   * @return {@link VerifyDocumentByQrCommand} listo para ser procesado por el caso de uso.
   */
  public VerifyDocumentByQrCommand toCommand() {
    return new VerifyDocumentByQrCommand(
        docId, relationshipId, qrUrl, location, tenantId, tenantSegment);
  }
}
