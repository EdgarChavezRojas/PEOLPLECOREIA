package com.solveria.core.dossier.application.command;

import java.util.UUID;

/**
 * Comando inmutable que encapsula la petición de validación de un título universitario o de
 * postgrado mediante la lectura del contenido de un código QR oficial boliviano.
 *
 * <p>Sigue el principio de Command Pattern sobre arquitectura Hexagonal / DDD. Todos los campos son
 * expuestos únicamente mediante los accessors del Record, garantizando la inmutabilidad del comando
 * una vez construido.
 *
 * @param docId Identificador del registro existente en el Digital Kardex.
 * @param relationshipId Identificador del vínculo laboral (EmploymentRelationship).
 * @param qrUrl URL oficial obtenida del código QR del título académico.
 * @param location Ubicación geográfica del proceso (debe ser "Santa Cruz, Bolivia").
 * @param tenantId Identificador del tenant (organización) en la plataforma.
 * @param tenantSegment Segmento del tenant (e.g., EDUCACION, RETAIL, ONG, CORPORATIVO).
 */
public record VerifyDocumentByQrCommand(
    UUID docId,
    UUID relationshipId,
    String qrUrl,
    String location,
    UUID tenantId,
    String tenantSegment) {}
