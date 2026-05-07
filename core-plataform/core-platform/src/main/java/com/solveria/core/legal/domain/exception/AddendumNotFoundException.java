package com.solveria.core.legal.domain.exception;

import com.solveria.core.shared.exceptions.DomainException;
import java.util.Map;
import java.util.UUID;

public class AddendumNotFoundException extends DomainException {

  public AddendumNotFoundException(UUID addendumId) {
    super("ADDENDUM_NOT_FOUND", Map.of("addendumId", addendumId), null);
  }
}
