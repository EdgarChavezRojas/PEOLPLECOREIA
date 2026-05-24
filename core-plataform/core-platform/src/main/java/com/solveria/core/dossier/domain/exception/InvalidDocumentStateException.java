package com.solveria.core.dossier.domain.exception;

import com.solveria.core.shared.exceptions.DomainException;
import java.util.Map;

public class InvalidDocumentStateException extends DomainException {

  public InvalidDocumentStateException(String reason) {
    super("INVALID_DOCUMENT_STATE", Map.of("reason", reason), null);
  }
}
