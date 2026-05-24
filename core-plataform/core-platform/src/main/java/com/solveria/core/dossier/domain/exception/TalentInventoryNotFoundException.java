package com.solveria.core.dossier.domain.exception;

import com.solveria.core.shared.exceptions.DomainException;
import java.util.Map;
import java.util.UUID;

public class TalentInventoryNotFoundException extends DomainException {

  public TalentInventoryNotFoundException(UUID relationshipId) {
    super("TALENT_INVENTORY_NOT_FOUND", Map.of("relationshipId", relationshipId), null);
  }
}
