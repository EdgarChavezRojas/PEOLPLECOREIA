package com.solveria.core.dossier.domain.exception;

import com.solveria.core.shared.exceptions.DomainException;
import java.util.Map;
import java.util.UUID;

public class AssignedAssetNotFoundException extends DomainException {

  public AssignedAssetNotFoundException(UUID assignmentId) {
    super("ASSET_ASSIGNMENT_NOT_FOUND", Map.of("assignmentId", assignmentId), null);
  }
}
