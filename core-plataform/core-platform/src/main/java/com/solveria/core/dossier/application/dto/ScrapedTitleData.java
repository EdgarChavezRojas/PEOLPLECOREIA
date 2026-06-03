package com.solveria.core.dossier.application.dto;

/**
 * DTO de transporte inmutable (Value Object de capa de aplicación) que contiene los datos extraídos
 * del portal web oficial al que apunta el código QR del título académico.
 *
 * <p>El scraper de infraestructura ({@code JsoupQrDocumentScraperAdapter}) produce esta instancia;
 * la capa de aplicación la consume para el cruce de identidad sin conocer los detalles de
 * implementación de la extracción HTML.
 *
 * @param fullName Nombre completo del titular del título, extraído del portal oficial.
 * @param ci Cédula de Identidad (CI) del titular, extraída del portal oficial.
 * @param degreeName Nombre del grado o título académico reconocido.
 * @param institution Nombre de la institución educativa que emitió el título.
 */
public record ScrapedTitleData(String fullName, String ci, String degreeName, String institution) {}
