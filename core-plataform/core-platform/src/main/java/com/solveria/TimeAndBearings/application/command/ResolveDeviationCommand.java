package com.solveria.TimeAndBearings.application.command;

import com.solveria.TimeAndBearings.domain.model.enums.ResolutionStatus;
import java.util.UUID;

/**
 * Command for resolving a deviation.
 *
 * @param ledgerId AttendanceLedger containing the deviation.
 * @param deviationId TimeDeviationRecord to resolve.
 * @param actorId UUID of the MSS/Analyst performing the action.
 * @param newStatus Target resolution status (APPROVED, REJECTED, OVERRIDDEN_BY_MANAGER).
 * @param reasonNote Mandatory note. Min 20 characters for APPROVED/OVERRIDDEN (P-TM32).
 * @param secondaryApproverId Second-level approver UUID if required (P-TM32, retroactivity >48h).
 * @param tenantId Multi-tenant partition.
 */
public record ResolveDeviationCommand(
    UUID ledgerId,
    UUID deviationId,
    UUID actorId,
    ResolutionStatus newStatus,
    String reasonNote,
    UUID secondaryApproverId,
    UUID tenantId) {}
