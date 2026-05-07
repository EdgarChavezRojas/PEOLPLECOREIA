package com.solveria.core.dossier.domain.exception;

import com.solveria.core.shared.exceptions.DomainException;
import java.util.Map;
import java.util.UUID;

public class DocumentNotFoundException extends DomainException {

  public DocumentNotFoundException(UUID docId) {
    super("DOCUMENT_NOT_FOUND", Map.of("docId", docId), null);
  }
}
