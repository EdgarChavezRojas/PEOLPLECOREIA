package com.solveria.core.dossier.domain.exception;

import com.solveria.core.shared.exceptions.DomainException;
import java.util.Map;

/**
 * Excepción de dominio lanzada cuando el adaptador de infraestructura no puede acceder a la URL del
 * código QR o el contenido HTML devuelto no puede ser parseado correctamente para extraer los datos
 * del título académico.
 *
 * <p>Esta excepción actúa como barrera anti-corrupción entre los errores técnicos de Jsoup/HTTP y
 * el lenguaje ubicuo del dominio de Document Compliance.
 */
public class QrScrapingException extends DomainException {

  /**
   * @param url URL que originó el error de scraping.
   * @param cause Causa raíz técnica (IOException, SocketTimeoutException, etc.).
   */
  public QrScrapingException(String url, Throwable cause) {
    super("QR_SCRAPING_FAILED", Map.of("url", url != null ? url : ""), String.valueOf(cause));
  }
}
