package com.solveria.TimeAndBearings.application.port.inbound;

import com.solveria.TimeAndBearings.application.command.ResolveDeviationCommand;
import com.solveria.TimeAndBearings.domain.model.entity.TimeDeviationRecord;
import java.util.UUID;

/**
 * Inbound Port: Exception Handling (WF-TM02).
 *
 * <p>Provides the structured channel for MSS/Analyst to review, justify, approve, or reject each
 * TimeDeviationRecord. Enforces P-TM31 (justification window), P-TM32 (retroactive punch controls),
 * and P-TM33 (immutability post-close).
 */
public interface ExceptionHandlingPort {

  /**
   * Resolves a TimeDeviationRecord with a manager decision (WF-TM02 steps 3–5).
   *
   * @param command Resolution intent from the MSS panel.
   * @return The updated TimeDeviationRecord.
   */
  TimeDeviationRecord resolveDeviation(ResolveDeviationCommand command);

  /**
   * Triggers automatic closure of all PENDING deviations that have exceeded the P-TM31
   * justification window (72h labor hours / 24h for NO_SHOW on closing day). Called by the CRON
   * scheduler (WF-TM03).
   *
   * @param tenantId Tenant scope for the batch operation.
   */
  void autoCloseExpiredDeviations(UUID tenantId);
}
