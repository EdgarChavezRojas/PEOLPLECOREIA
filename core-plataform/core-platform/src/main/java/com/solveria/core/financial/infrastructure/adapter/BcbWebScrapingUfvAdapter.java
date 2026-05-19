package com.solveria.core.financial.infrastructure.adapter;

import com.solveria.core.financial.application.port.UfvQuotationPort;
import com.solveria.core.financial.domain.model.vo.UfvProviderUnavailableException;

import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

/**
 * Adapter: Descarga el PDF anual del BCB y extrae la matriz de UFVs.
 * Incluye lógica de auto-actualización si faltan datos de fechas recientes.
 */
@Slf4j
@Component
public class BcbWebScrapingUfvAdapter implements UfvQuotationPort {

  private static final String BCB_PDF_URL_TEMPLATE = "https://www.bcb.gob.bo/librerias/indicadores/ufv/anualpdf.php?gestion=%d";
  private static final Pattern UFV_PATTERN = Pattern.compile("(\\d+[.,]\\d{5})");

  // Caché de UFVs. Clave: Fecha exacta, Valor: UFV
  private final Map<LocalDate, BigDecimal> cache = new ConcurrentHashMap<>();

  // Registro de CUÁNDO fue la última vez que descargamos el PDF de un año específico.
  // Clave: Año, Valor: Fecha de la última descarga
  private final Map<Integer, LocalDate> lastDownloadDatePerYear = new ConcurrentHashMap<>();

  @Override
  public BigDecimal getUfvValue(LocalDate date) throws UfvProviderUnavailableException {
    int year = date.getYear();

    // 1. Buscamos primero en la memoria rápida
    BigDecimal ufvValue = cache.get(date);

    // 2. Si NO existe el dato, evaluamos si debemos volver a descargar el PDF
    if (ufvValue == null) {
      LocalDate lastDownload = lastDownloadDatePerYear.get(year);

      // Regla de negocio: Si nunca lo hemos descargado, o si lo descargamos en un día
      // anterior a HOY, volvemos a descargarlo para buscar la versión más reciente.
      // (Esto evita que descarguemos el PDF 1000 veces el mismo día si alguien pide una fecha del futuro).
      if (lastDownload == null || lastDownload.isBefore(LocalDate.now())) {
        log.info("event=UFV_CACHE_MISS date={} action=FORCING_PDF_REFRESH", date);
        loadYearFromPdf(year);

        // Volvemos a intentar sacar el dato después de la actualización
        ufvValue = cache.get(date);
      }
    }

    // 3. Si después de descargar el PDF fresco de internet sigue sin existir,
    // significa que el BCB realmente aún no publica esa fecha (ej. pedir la UFV de diciembre en mayo).
    if (ufvValue == null) {
      throw new UfvProviderUnavailableException(
              "El BCB aún no ha publicado el valor de la UFV para la fecha: " + date);
    }

    return ufvValue;
  }

  /**
   * Descarga el PDF del año solicitado y mapea (o sobrescribe) todos sus valores en la caché.
   */
  private synchronized void loadYearFromPdf(int year) throws UfvProviderUnavailableException {
    // Doble chequeo por si múltiples hilos llegaron aquí al mismo tiempo
    LocalDate lastDownload = lastDownloadDatePerYear.get(year);
    if (lastDownload != null && lastDownload.isEqual(LocalDate.now())) {
      return;
    }

    String urlString = String.format(BCB_PDF_URL_TEMPLATE, year);
    log.info("Conectando al BCB para descargar PDF del año {}: {}", year, urlString);

    try (InputStream in = URI.create(urlString).toURL().openStream();
         PDDocument document = PDDocument.load(in)) {

      PDFTextStripper stripper = new PDFTextStripper();
      String pdfText = stripper.getText(document);

      parsePdfTextToCache(pdfText, year);

      // Registramos que HOY ya descargamos la versión más reciente de este año
      lastDownloadDatePerYear.put(year, LocalDate.now());
      log.info("event=UFV_PDF_LOADED year={} total_records_in_cache={}", year, cache.size());

    } catch (Exception e) {
      log.error("Error al descargar/procesar PDF del año {}: {}", year, e.getMessage());
      throw new UfvProviderUnavailableException("Fallo al actualizar el PDF del BCB para " + year, e);
    }
  }

  /**
   * Procesa el texto extraído del PDF línea por línea y lo inyecta en la caché.
   */
  private void parsePdfTextToCache(String pdfText, int year) {
    String[] lines = pdfText.split("\\r?\\n");

    for (String line : lines) {
      line = line.trim();
      if (!line.matches("^([1-9]|[12]\\d|3[01])\\b.*")) continue;

      int day = Integer.parseInt(line.split("\\s+")[0]);
      Matcher matcher = UFV_PATTERN.matcher(line);
      int month = 1;

      while (matcher.find() && month <= 12) {
        BigDecimal ufv = new BigDecimal(matcher.group(1).replace(",", "."));
        try {
          LocalDate date = LocalDate.of(year, month, day);
          cache.put(date, ufv); // put() sobrescribe si ya existía, lo cual es perfecto
        } catch (DateTimeException ignored) {}
        month++;
      }
    }
  }
}
