package com.solveria.core.dossier.domain.exception;

import com.solveria.core.shared.exceptions.DomainException;
import java.util.Map;

public class InvalidLocalizationException extends DomainException {

  public InvalidLocalizationException(String location) {
    super("INVALID_LOCALIZATION", Map.of("location", location), null);
  }
}
