package com.solveria.core.accruals.domain.exception;

import com.solveria.core.shared.exceptions.DomainException;
import java.util.Map;

public class InvalidLeaveStateException extends DomainException {

  public InvalidLeaveStateException(String reason) {
    super("INVALID_LEAVE_STATE", Map.of("reason", reason), reason);
  }
}
