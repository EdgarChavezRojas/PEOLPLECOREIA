package com.solveria.core.accruals.domain.exception;

import com.solveria.core.shared.exceptions.DomainException;
import java.util.Map;
import java.util.UUID;

public class QuinquenioProvisionNotFoundException extends DomainException {

  public QuinquenioProvisionNotFoundException(UUID relationshipId) {
    super("QUINQUENIO_PROVISION_NOT_FOUND", Map.of("relationshipId", relationshipId), null);
  }
}
