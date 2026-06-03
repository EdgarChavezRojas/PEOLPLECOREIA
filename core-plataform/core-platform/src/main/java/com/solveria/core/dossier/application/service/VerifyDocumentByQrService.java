package com.solveria.core.dossier.application.service;

import com.solveria.core.dossier.application.command.ComplianceDecision;
import com.solveria.core.dossier.application.command.VerifyDocumentByQrCommand;
import com.solveria.core.dossier.application.command.VerifyDocumentComplianceCommand;
import com.solveria.core.dossier.application.dto.ScrapedTitleData;
import com.solveria.core.dossier.application.port.QrDocumentScraperPort;
import com.solveria.core.dossier.application.usecase.VerifyDocumentByQrUseCase;
import com.solveria.core.dossier.application.usecase.VerifyDocumentComplianceUseCase;
import com.solveria.core.dossier.domain.model.DocumentRecord;
import com.solveria.core.dossier.domain.model.vo.DocumentCategory;
import com.solveria.core.dossier.domain.policy.LocalizationPolicy;
import com.solveria.core.workforce.application.port.PersonRepositoryPort;
import com.solveria.core.workforce.domain.model.Person;
import java.time.LocalDateTime;
import java.util.Locale;
import org.springframework.stereotype.Service;

/**
 * Implementación del caso de uso {@link VerifyDocumentByQrUseCase}.
 *
 * <p>Este servicio de aplicación orquesta el flujo completo de validación automatizada de títulos
 * universitarios y de postgrado en Santa Cruz, Bolivia, mediante la lectura del contenido de un
 * código QR oficial. Es la única clase que conoce la secuencia de pasos del proceso; los detalles
 * técnicos quedan delegados en puertos.
 *
 * <h3>Flujo de negocio implementado:</h3>
 *
 * <ol>
 *   <li><b>Política de Origen Seguro:</b> Whitelist de dominios .gob.bo / .edu.bo
 *   <li><b>Localización:</b> Restricción territorial Santa Cruz, Bolivia
 *   <li><b>Scraping:</b> Extracción de datos del portal oficial vía QrDocumentScraperPort
 *   <li><b>Cruce de Identidad:</b> Búsqueda por CI y validación de nombre (CI boliviana)
 *   <li><b>Aprobación:</b> Delega en VerifyDocumentComplianceUseCase con decisión APPROVE
 * </ol>
 *
 * <p>Sigue el principio de dependencia invertida: solo depende de interfaces (puertos), nunca de
 * adaptadores concretos.
 */
@Service
public class VerifyDocumentByQrService implements VerifyDocumentByQrUseCase {

  /** Sufijos de dominios gubernamentales/educativos bolivianos reconocidos como seguros. */
  private static final String[] SAFE_BOLIVIAN_DOMAIN_SUFFIXES = {".gob.bo", ".edu.bo"};

  private final QrDocumentScraperPort qrDocumentScraperPort;
  private final PersonRepositoryPort personRepositoryPort;
  private final VerifyDocumentComplianceUseCase verifyDocumentComplianceUseCase;

  /**
   * Constructor para inyección de dependencias via constructor (patrón recomendado en Arquitectura
   * Hexagonal — permite testabilidad sin Spring en la capa de aplicación).
   *
   * @param qrDocumentScraperPort Puerto de scraping de portales oficiales bolivianos.
   * @param personRepositoryPort Puerto de acceso al repositorio de personas (CI lookup).
   * @param verifyDocumentComplianceUseCase Caso de uso de cumplimiento documental del Kardex.
   */
  public VerifyDocumentByQrService(
      QrDocumentScraperPort qrDocumentScraperPort,
      PersonRepositoryPort personRepositoryPort,
      VerifyDocumentComplianceUseCase verifyDocumentComplianceUseCase) {
    this.qrDocumentScraperPort = qrDocumentScraperPort;
    this.personRepositoryPort = personRepositoryPort;
    this.verifyDocumentComplianceUseCase = verifyDocumentComplianceUseCase;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Ejecuta el flujo completo de validación automática de título académico por QR. Si la
   * identidad es confirmada con éxito, aprueba el DocumentRecord en el Digital Kardex.
   */
  @Override
  public DocumentRecord handle(VerifyDocumentByQrCommand command) {

    // ── PASO A: Política de Origen Seguro (Whitelisting de dominios bolivianos) ──────────────
    validateSecureQrUrl(command.qrUrl());

    // ── PASO B: Localización — Invariante territorial Santa Cruz, Bolivia ─────────────────────
    LocalizationPolicy.requireSantaCruz(command.location());

    // ── PASO C: Scraping — Extracción de datos desde el portal oficial ───────────────────────
    ScrapedTitleData scrapedData = qrDocumentScraperPort.scrape(command.qrUrl());

    // ── PASO D: Cruce de Identidad Civil (Deduplicación y Seguridad) ─────────────────────────
    Person person = resolveAndVerifyIdentity(scrapedData);

    // ── PASO E: Aprobación del documento en el Digital Kardex ────────────────────────────────
    return approveDocumentInKardex(command, scrapedData, person);
  }

  // ═══════════════════════════════════════════════════════════════════════════════════════════
  //  MÉTODOS PRIVADOS DE POLÍTICA DE NEGOCIO
  // ═══════════════════════════════════════════════════════════════════════════════════════════

  /**
   * Política de Origen Seguro: verifica que la URL del QR pertenezca exclusivamente a un dominio
   * gubernamental (.gob.bo) o educativo (.edu.bo) boliviano.
   *
   * @param qrUrl URL a validar.
   * @throws IllegalArgumentException si la URL es nula, en blanco, o no pertenece a un dominio
   *     seguro boliviano reconocido.
   */
  private void validateSecureQrUrl(String qrUrl) {
    if (qrUrl == null || qrUrl.isBlank()) {
      throw new IllegalArgumentException(
          "URL de QR no segura o inválida para el territorio boliviano");
    }

    // Normalización: ignorar mayúsculas, eliminar parámetros de query para el chequeo de dominio
    String normalizedUrl = qrUrl.strip().toLowerCase(Locale.ROOT);

    boolean isSafe = false;
    for (String suffix : SAFE_BOLIVIAN_DOMAIN_SUFFIXES) {
      // Extrae el host de la URL para verificar el sufijo de dominio correctamente
      if (extractHost(normalizedUrl).endsWith(suffix)) {
        isSafe = true;
        break;
      }
    }

    if (!isSafe) {
      throw new IllegalArgumentException(
          "URL de QR no segura o inválida para el territorio boliviano");
    }
  }

  /**
   * Extrae el nombre del host de una URL normalizada en minúsculas. Soporta URLs con o sin esquema
   * (http/https).
   *
   * @param normalizedUrl URL en minúsculas.
   * @return Nombre del host sin ruta ni parámetros.
   */
  private String extractHost(String normalizedUrl) {
    String withoutScheme = normalizedUrl;
    if (withoutScheme.startsWith("https://")) {
      withoutScheme = withoutScheme.substring(8);
    } else if (withoutScheme.startsWith("http://")) {
      withoutScheme = withoutScheme.substring(7);
    }
    // Eliminar ruta, query y fragmento
    int slashIdx = withoutScheme.indexOf('/');
    if (slashIdx != -1) {
      withoutScheme = withoutScheme.substring(0, slashIdx);
    }
    int queryIdx = withoutScheme.indexOf('?');
    if (queryIdx != -1) {
      withoutScheme = withoutScheme.substring(0, queryIdx);
    }
    return withoutScheme;
  }

  /**
   * Cruce de Identidad: busca a la persona en el sistema usando el CI extraído del QR y verifica
   * que el nombre completo coincida con los datos registrados en la plataforma.
   *
   * <p>La comparación de nombres ignora mayúsculas/minúsculas y espacios adicionales para evitar
   * falsos negativos por diferencias tipográficas.
   *
   * @param scrapedData Datos extraídos del portal oficial.
   * @return La entidad {@link Person} verificada.
   * @throws IllegalArgumentException si la persona no existe en el sistema o si el nombre no
   *     coincide con el registrado.
   */
  private Person resolveAndVerifyIdentity(ScrapedTitleData scrapedData) {
    // Búsqueda por CI boliviana (campo DNI en Person = cédula de identidad)
    Person person =
        personRepositoryPort
            .findByCi(scrapedData.ci())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        String.format(
                            "No se encontró ninguna persona con CI '%s' en el sistema PeopleCoreIA."
                                + " Verifique que el empleado esté registrado antes de validar su título.",
                            scrapedData.ci())));

    // Comparación normalizada de nombres: ignorar mayúsculas y espacios extra
    String systemName = normalize(person.getFullName());
    String scrapedName = normalize(scrapedData.fullName());

    if (!systemName.equals(scrapedName)) {
      throw new IllegalArgumentException(
          String.format(
              "La identidad civil no pudo ser confirmada. "
                  + "El nombre en el portal oficial ('%s') no coincide con el nombre "
                  + "registrado en el sistema ('%s') para el CI '%s'.",
              scrapedData.fullName(), person.getFullName(), scrapedData.ci()));
    }

    return person;
  }

  /**
   * Normaliza un nombre para comparación: convierte a minúsculas y colapsa espacios múltiples.
   *
   * @param name Nombre a normalizar.
   * @return Cadena normalizada para comparación segura.
   */
  private String normalize(String name) {
    if (name == null) {
      return "";
    }
    return name.strip().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
  }

  /**
   * Construye el {@link VerifyDocumentComplianceCommand} con los datos recopilados del QR y delega
   * en {@link VerifyDocumentComplianceUseCase} para persistir la aprobación inmutable del título en
   * el Digital Kardex.
   *
   * <p>El campo {@code docType} se establece con el nombre del grado extraído del QR. El documento
   * se clasifica automáticamente como {@code DocumentCategory.ACADEMIC} y se marca como {@code
   * critical = true} (los títulos habilitantes son críticos para la asignación de materias en el
   * sector educación, conforme a P7 de la documentación).
   *
   * @param command Comando original del caso de uso QR.
   * @param scrapedData Datos del título extraídos del portal oficial.
   * @param person Persona verificada en el sistema.
   * @return {@link DocumentRecord} con estado APPROVED persistido en el Kardex.
   */
  private DocumentRecord approveDocumentInKardex(
      VerifyDocumentByQrCommand command, ScrapedTitleData scrapedData, Person person) {

    VerifyDocumentComplianceCommand complianceCommand =
        new VerifyDocumentComplianceCommand(
            command.docId(), // docId  — registro existente en el Kardex
            command.relationshipId(), // relationshipId — vínculo laboral
            DocumentCategory.ACADEMIC, // docCategory — título académico
            scrapedData.degreeName(), // docType — nombre del grado (Ej: "Lic. Derecho")
            true, // critical — títulos son críticos en sector educación
            null, // storageId — no se sube archivo en este flujo
            null, // fileName  — ídem
            null, // fileContent — ídem
            null, // expiryDate — los títulos no caducan
            ComplianceDecision.APPROVE, // decision  — aprobación automática confirmada
            person.getPersonId(), // reviewerId — la persona verificada actúa como origen
            null, // rejectReason — N/A en aprobación
            LocalDateTime.now(), // reviewDate — fecha/hora de aprobación automática
            command.location(), // location — heredada del comando QR
            command.tenantId(), // tenantId
            command.tenantSegment()); // tenantSegment

    return verifyDocumentComplianceUseCase.handle(complianceCommand);
  }
}
