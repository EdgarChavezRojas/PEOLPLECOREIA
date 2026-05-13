package com.solveria.core.financial.infrastructure.adapter;

import com.solveria.core.financial.application.port.UfvQuotationPort;
import com.solveria.core.financial.domain.model.vo.UfvProviderUnavailableException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

/**
 * Adapter: Implementa UfvQuotationPort mediante Web Scraping (Jsoup) al sitio del
 * Banco Central de Bolivia.
 *
 * <p>Estrategia:
 * <ul>
 *   <li>Jsoup.connect() con timeout de 5 segundos.</li>
 *   <li>Caché en memoria (ConcurrentHashMap) para evitar peticiones duplicadas
 *       durante ejecuciones masivas de la misma fecha.</li>
 *   <li>Si falla (timeout, cambio de DOM, error de red), lanza
 *       UfvProviderUnavailableException.</li>
 * </ul>
 */
@Slf4j
@Component
public class BcbWebScrapingUfvAdapter implements UfvQuotationPort {

  private static final String BCB_UFV_URL =
      "https://www.bcb.gob.bo/librerias/indicadores/ufv/ufv.php";

  private static final int TIMEOUT_MS = 5_000;

  private static final DateTimeFormatter BCB_DATE_FORMAT =
      DateTimeFormatter.ofPattern("dd/MM/yyyy");

  /** Caché en memoria para evitar peticiones HTTP duplicadas por fecha. */
  private final Map<LocalDate, BigDecimal> cache = new ConcurrentHashMap<>();

  @Override
  public BigDecimal getUfvValue(LocalDate date) throws UfvProviderUnavailableException {
    log.info("event=UFV_QUOTATION_REQUEST date={}", date);

    BigDecimal cached = cache.get(date);
    if (cached != null) {
      log.info("event=UFV_CACHE_HIT date={} value={}", date, cached);
      return cached;
    }

    try {
      String formattedDate = date.format(BCB_DATE_FORMAT);

      Document doc =
          Jsoup.connect(BCB_UFV_URL)
              .data("fecha", formattedDate)
              .timeout(TIMEOUT_MS)
              .post();

      Element resultElement = doc.selectFirst("div.resultado, span.ufv-valor, #resultado, td.valor");

      String rawValue;
      if (resultElement != null) {
        rawValue = resultElement.text().trim();
      } else {
        rawValue = extractFallback(doc);
      }

      if (rawValue == null || rawValue.isBlank()) {
        throw new UfvProviderUnavailableException(
            "No se encontró el valor UFV en el DOM del BCB para fecha: " + formattedDate);
      }

      String sanitized = rawValue.replaceAll("[^0-9.,]", "").replace(",", ".");
      BigDecimal ufvValue = new BigDecimal(sanitized);

      cache.put(date, ufvValue);
      log.info("event=UFV_QUOTATION_SUCCESS date={} value={}", date, ufvValue);
      return ufvValue;

    } catch (UfvProviderUnavailableException ex) {
      throw ex;
    } catch (NumberFormatException ex) {
      log.error("event=UFV_PARSE_ERROR date={} error={}", date, ex.getMessage());
      throw new UfvProviderUnavailableException(
          "Valor UFV no numérico en la respuesta del BCB para fecha: " + date, ex);
    } catch (Exception ex) {
      log.error("event=UFV_PROVIDER_UNAVAILABLE date={} error={}", date, ex.getMessage());
      throw new UfvProviderUnavailableException(
          "El servicio web del BCB no respondió (timeout 5s) para fecha: " + date, ex);
    }
  }

  /**
   * Estrategia fallback: busca cualquier elemento del body que contenga un valor
   * numérico con formato UFV (ej. "2.xxxxx").
   */
  private String extractFallback(Document doc) {
    for (Element el : doc.body().getAllElements()) {
      String text = el.ownText().trim();
      if (text.matches("\\d+[.,]\\d{2,}")) {
        return text;
      }
    }
    return null;
  }
}
