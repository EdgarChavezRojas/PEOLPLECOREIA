package com.solveria.core.dossier.application.usecase;

import com.solveria.core.dossier.application.command.VerifyDocumentByQrCommand;
import com.solveria.core.dossier.domain.model.DocumentRecord;

/**
 * Puerto de Entrada (Inbound Port / Use Case) para el flujo de validación automatizada de títulos
 * universitarios y de postgrado mediante la lectura de un código QR oficial.
 *
 * <p>Este puerto define el contrato de la capa de aplicación. El controlador REST ({@code
 * VerifyDocumentByQrController}) depende exclusivamente de esta interfaz, nunca de la
 * implementación concreta, respetando el principio de inversión de dependencia.
 *
 * <p>El caso de uso orquesta las siguientes políticas de dominio:
 *
 * <ol>
 *   <li>Política de Origen Seguro (whitelist .gob.bo / .edu.bo)
 *   <li>Política de Territorialidad ({@code LocalizationPolicy.requireSantaCruz})
 *   <li>Scraping del portal oficial via {@code QrDocumentScraperPort}
 *   <li>Cruce y validación de identidad civil via {@code PersonRepositoryPort}
 *   <li>Aprobación automática del documento en el Digital Kardex
 * </ol>
 */
public interface VerifyDocumentByQrUseCase {

  /**
   * Ejecuta la verificación y aprobación automática del título académico.
   *
   * @param command Comando inmutable con todos los parámetros necesarios para iniciar el flujo de
   *     validación por QR.
   * @return {@link DocumentRecord} actualizado con estado {@code APPROVED} en el Digital Kardex del
   *     sistema PeopleCoreIA.
   * @throws IllegalArgumentException si la URL del QR no pertenece a un dominio
   *     gubernamental/educativo boliviano seguro, o si la identidad civil no puede ser verificada.
   * @throws com.solveria.core.dossier.domain.exception.InvalidLocalizationException si la
   *     localización no corresponde a Santa Cruz, Bolivia.
   * @throws com.solveria.core.dossier.domain.exception.QrScrapingException si el portal oficial no
   *     es accesible o no puede ser parseado.
   */
  DocumentRecord handle(VerifyDocumentByQrCommand command);
}
