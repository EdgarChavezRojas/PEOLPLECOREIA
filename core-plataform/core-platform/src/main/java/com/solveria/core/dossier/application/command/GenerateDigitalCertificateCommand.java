package com.solveria.core.dossier.application.command;

import com.solveria.core.dossier.domain.model.vo.DocumentCategory;
import java.time.LocalDate;
import java.util.UUID;

public record GenerateDigitalCertificateCommand(
    UUID relationshipId,
    String docType,
    boolean critical,
    String fileName,
    byte[] content,
    LocalDate expiryDate,
    String location) {

  public GenerateDigitalCertificateCommand {
    if (docType == null || docType.isBlank()) {
      throw new IllegalArgumentException("docType es requerido");
    }
    if (fileName == null || fileName.isBlank()) {
      throw new IllegalArgumentException("fileName es requerido");
    }
    if (content == null || content.length == 0) {
      throw new IllegalArgumentException("content es requerido");
    }
  }

  public DocumentCategory category() {
    return DocumentCategory.LEGAL;
  }
}
