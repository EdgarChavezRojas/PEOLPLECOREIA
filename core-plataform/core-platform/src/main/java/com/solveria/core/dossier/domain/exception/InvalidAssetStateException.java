package com.solveria.core.dossier.domain.exception;

import com.solveria.core.shared.exceptions.DomainException;
import java.util.Map;

public class InvalidAssetStateException extends DomainException {

  public InvalidAssetStateException(String reason) {
    super("INVALID_ASSET_STATE", Map.of("reason", reason), null);
  }
}
