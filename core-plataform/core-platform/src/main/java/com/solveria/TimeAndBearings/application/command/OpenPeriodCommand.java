package com.solveria.TimeAndBearings.application.command;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Comando para abrir un nuevo TimesheetPeriod.
 *
 * @param tenantId FK opaca al Tenant (BC-01)
 * @param orgUnitId FK opaca a la OrgUnit (BC-01)
 * @param periodStart primer dia del periodo
 * @param periodEnd ultimo dia del periodo
 * @param periodTypeId tipo de periodo: "WEEKLY", "BIWEEKLY" o "MONTHLY"
 */
public record OpenPeriodCommand(
    UUID tenantId,
    UUID orgUnitId,
    LocalDate periodStart,
    LocalDate periodEnd,
    String periodTypeId) {}
