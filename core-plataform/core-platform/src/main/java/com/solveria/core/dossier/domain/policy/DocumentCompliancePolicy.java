package com.solveria.core.dossier.domain.policy;

import com.solveria.core.dossier.domain.exception.InvalidDocumentStateException;
import com.solveria.core.dossier.domain.model.vo.DocumentCategory;
import java.time.LocalDate;

public final class DocumentCompliancePolicy {

  private static final String DOC_TYPE_HEALTH_CARD = "Carnet Sanitario";
  private static final String DOC_TYPE_ACADEMIC_TITLE = "Titulo Academico";

  private DocumentCompliancePolicy() {}

  public static void requireCriticalDocumentType(
      String tenantSegment, DocumentCategory category, String docType, boolean isCritical) {
    if (!isCritical) {
      return;
    }
    if (tenantSegment == null || tenantSegment.isBlank()) {
      return;
    }
    String segment = tenantSegment.trim().toUpperCase();
    if ("RETAIL".equals(segment) && category == DocumentCategory.HEALTH) {
      if (!DOC_TYPE_HEALTH_CARD.equalsIgnoreCase(docType)) {
        throw new InvalidDocumentStateException(
            "DocType requerido para Retail: " + DOC_TYPE_HEALTH_CARD);
      }
    }
    if ("EDUCACION".equals(segment) && category == DocumentCategory.ACADEMIC) {
      if (!DOC_TYPE_ACADEMIC_TITLE.equalsIgnoreCase(docType)) {
        throw new InvalidDocumentStateException(
            "DocType requerido para Educacion: " + DOC_TYPE_ACADEMIC_TITLE);
      }
    }
  }

  public static boolean isHealthCardExpiringSoon(
      String docType, LocalDate expiryDate, LocalDate today) {
    if (docType == null || expiryDate == null || today == null) {
      return false;
    }
    if (!DOC_TYPE_HEALTH_CARD.equalsIgnoreCase(docType)) {
      return false;
    }
    LocalDate warningDate = today.plusDays(30);
    return !expiryDate.isAfter(warningDate);
  }
}
