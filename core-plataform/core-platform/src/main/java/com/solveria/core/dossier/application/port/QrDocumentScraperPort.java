package com.solveria.core.dossier.application.port;

import com.solveria.core.dossier.application.dto.ScrapedTitleData;

/**
 * Puerto de Salida (Outbound Port) para el scraping de datos académicos desde portales web
 * oficiales bolivianos referenciados por códigos QR.
 *
 * <p>La capa de Aplicación depende exclusivamente de esta interfaz, manteniendo la separación entre
 * la lógica de negocio y los detalles técnicos de extracción de contenido HTML (implementados en
 * {@code JsoupQrDocumentScraperAdapter}).
 *
 * <p>Principio clave: esta interfaz pertenece a la capa de aplicación y define el contrato; la
 * implementación concreta vive en la capa de infraestructura.
 */
public interface QrDocumentScraperPort {

  /**
   * Realiza el scraping del portal web oficial al que apunta la URL del QR y devuelve los datos del
   * título académico en una estructura inmutable.
   *
   * @param url URL segura (.gob.bo o .edu.bo) del portal de verificación oficial.
   * @return con los datos extraídos del documento HTML.
   * @throws com.solveria.core.dossier.domain.exception.QrScrapingException si la URL no es
   *     accesible o el contenido no puede ser parseado correctamente.
   */
  ScrapedTitleData scrape(String url);
}
