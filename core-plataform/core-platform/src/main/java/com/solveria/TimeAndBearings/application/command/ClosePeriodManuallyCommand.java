package com.solveria.TimeAndBearings.application.command;

import java.util.UUID;

/**
 * Comando para el cierre manual de un periodo por un actor humano.
 *
 * @param periodId identificador del periodo a cerrar
 * @param closedBy UUID del MSS o Analista que ejecuta el cierre
 */
public record ClosePeriodManuallyCommand(UUID periodId, UUID closedBy) {}
