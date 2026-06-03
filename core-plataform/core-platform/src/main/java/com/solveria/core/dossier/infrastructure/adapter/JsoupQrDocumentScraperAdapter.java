package com.solveria.core.dossier.infrastructure.adapter;

import com.solveria.core.dossier.application.dto.ScrapedTitleData;
import com.solveria.core.dossier.application.port.QrDocumentScraperPort;
import com.solveria.core.dossier.domain.exception.QrScrapingException;
import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

/**
 * Adaptador de Infraestructura (Driven / Output Adapter) que implementa {@link
 * QrDocumentScraperPort} usando la librería <a href="https://jsoup.org/">Jsoup</a> para parsear el
 * HTML del portal oficial boliviano al que apunta el código QR de un título académico.
 *
 * <p><b>Responsabilidad única:</b> La única razón de cambio de esta clase es que el portal oficial
 * cambie su estructura HTML. La lógica de negocio (validación de identidad, políticas) permanece en
 * la capa de aplicación sin verse afectada.
 *
 * <h3>Estrategia de extracción de datos</h3>
 *
 * Los selectores CSS usados apuntan a los elementos semánticos más comunes en los portales de
 * verificación de títulos del MINEDU y universidades bolivianas (.edu.bo / .gob.bo). La estrategia
 * aplica un patrón de <em>fallback por múltiples selectores</em>: si el selector primario no
 * produce resultado, se intentan alternativas antes de devolver cadena vacía.
 *
 * <h3>Configuración de conexión</h3>
 *
 * <ul>
 *   <li>Timeout: 10 segundos (operación de scraping de portal externo).
 *   <li>User-Agent: emula navegador estándar para evitar bloqueos por bots.
 *   <li>followRedirects: {@code true} — los portales .gob.bo suelen redirigir a HTTPS.
 *   <li>ignoreHttpErrors: {@code false} — cualquier error HTTP lanzará {@link QrScrapingException}.
 * </ul>
 *
 * <p><b>Nota de arquitectura:</b> Esta clase usa {@code @Component} (no {@code @Service}) porque es
 * un adaptador técnico, no un servicio de dominio. El framework Spring la instancia e inyecta en
 * {@code VerifyDocumentByQrService} a través del puerto {@link QrDocumentScraperPort}.
 */
@Component
public class JsoupQrDocumentScraperAdapter implements QrDocumentScraperPort {

  /** Timeout de conexión y lectura en milisegundos (10 segundos). */
  private static final int CONNECTION_TIMEOUT_MS = 10_000;

  /**
   * User-Agent estándar para evitar bloqueos anti-bot en portales gubernamentales bolivianos. Los
   * portales .gob.bo generalmente aceptan solicitudes con User-Agent de navegador común.
   */
  private static final String USER_AGENT =
      "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
          + "AppleWebKit/537.36 (KHTML, like Gecko) "
          + "Chrome/124.0.0.0 Safari/537.36";

  /**
   * {@inheritDoc}
   *
   * <p>Realiza la conexión HTTP al portal oficial, descarga el HTML y extrae los campos semánticos
   * del título académico usando selectores CSS.
   *
   * @param url URL segura (.gob.bo o .edu.bo) — ya validada por el servicio de aplicación.
   * @return {@link ScrapedTitleData} poblado con los datos extraídos del portal.
   * @throws QrScrapingException si la URL no es accesible, el servidor devuelve un error HTTP, o el
   *     HTML no puede ser parseado.
   */
  @Override
  public ScrapedTitleData scrape(String url) {
    Document htmlDocument = fetchDocument(url);
    return extractTitleData(htmlDocument);
  }

  // ═══════════════════════════════════════════════════════════════════════════════════════════
  // MÉTODOS PRIVADOS
  // ═══════════════════════════════════════════════════════════════════════════════════════════

  /**
   * Establece la conexión HTTP con el portal oficial y descarga el documento HTML.
   *
   * @param url URL del portal de verificación oficial.
   * @return Documento HTML parseado por Jsoup.
   * @throws QrScrapingException si ocurre cualquier error de red o HTTP.
   */
  private Document fetchDocument(String url) {
    try {
      return Jsoup.connect(url)
          .userAgent(USER_AGENT)
          .timeout(CONNECTION_TIMEOUT_MS)
          .followRedirects(true)
          .ignoreHttpErrors(false)
          .get();
    } catch (IOException e) {
      throw new QrScrapingException(url, e);
    }
  }

  /**
   * Extrae los datos del título académico del documento HTML usando una estrategia de múltiples
   * selectores CSS para mayor resiliencia ante variaciones de estructura.
   *
   * <p>Selectores primarios basados en los patrones comunes de portales bolivianos (MINEDU, CEUB,
   * universidades autónomas):
   *
   * <ul>
   *   <li>{@code .nombre-titulado}, {@code #nombre-completo} — nombre del titular
   *   <li>{@code .ci-titular}, {@code #ci}, {@code [data-ci]} — cédula de identidad
   *   <li>{@code .nombre-titulo}, {@code #grado-academico} — nombre del título/grado
   *   <li>{@code .institucion}, {@code #universidad} — institución emisora
   * </ul>
   *
   * @param doc Documento HTML obtenido del portal oficial.
   * @return {@link ScrapedTitleData} con los campos extraídos (cadena vacía si no se encontró).
   */
  private ScrapedTitleData extractTitleData(Document doc) {
    String fullName =
        selectText(
            doc,
            ".nombre-titulado",
            "#nombre-completo",
            "[data-field='nombres']",
            "td:contains(Nombre) + td",
            ".data-nombre");

    String ci =
        selectText(
            doc,
            ".ci-titular",
            "#ci",
            "[data-ci]",
            "td:contains(C.I.) + td",
            "td:contains(Cédula) + td",
            ".data-ci");

    String degreeName =
        selectText(
            doc,
            ".nombre-titulo",
            "#grado-academico",
            "[data-field='titulo']",
            "td:contains(Título) + td",
            "td:contains(Grado) + td",
            ".data-titulo");

    String institution =
        selectText(
            doc,
            ".institucion",
            "#universidad",
            "[data-field='institucion']",
            "td:contains(Universidad) + td",
            "td:contains(Institución) + td",
            ".data-institucion");

    return new ScrapedTitleData(
        fullName.strip(), ci.strip(), degreeName.strip(), institution.strip());
  }

  /**
   * Intenta múltiples selectores CSS en orden y devuelve el texto del primer elemento encontrado.
   * Devuelve cadena vacía si ningún selector produce resultado.
   *
   * @param doc Documento HTML donde buscar.
   * @param selectors Selectores CSS en orden de prioridad (primario, fallbacks...).
   * @return Texto del primer elemento encontrado, o {@code ""} si ninguno aplica.
   */
  private String selectText(Document doc, String... selectors) {
    for (String selector : selectors) {
      try {
        Element element = doc.selectFirst(selector);
        if (element != null && !element.text().isBlank()) {
          return element.text();
        }
      } catch (Exception ignored) {
        // Selector inválido para este documento — continuar con el siguiente
      }
    }
    return "";
  }
}
