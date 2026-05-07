package com.solveria.core.dossier.domain.model.vo;

import java.time.LocalDate;
import java.util.UUID;

public record DocumentMetadata(
    UUID storageId, String fileName, String hashSha256, LocalDate expiryDate) {

  public DocumentMetadata {
    if (storageId == null) {
      throw new IllegalArgumentException("storageId es requerido");
    }
    if (fileName == null || fileName.isBlank()) {
      throw new IllegalArgumentException("fileName es requerido");
    }
    if (hashSha256 == null || hashSha256.length() != 64) {
      throw new IllegalArgumentException("hashSha256 debe tener 64 caracteres");
    }
  }
}
