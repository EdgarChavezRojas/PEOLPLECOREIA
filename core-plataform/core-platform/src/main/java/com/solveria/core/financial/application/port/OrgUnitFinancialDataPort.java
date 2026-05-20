package com.solveria.core.financial.application.port;

import com.solveria.core.financial.application.command.ImputeAnalyticCommand;
import java.util.UUID;

/**
 * Puerto secundario (outbound): Resuelve datos financieros de unidades organizativas.
 *
 * <p>Anti-Corruption Layer que mapea el contexto de Workforce (unidades organizativas) al contexto
 * financiero (fuentes de financiamiento, personas, períodos de imputación). Evita que el listener
 * construya comandos con datos fabricados.
 */
public interface OrgUnitFinancialDataPort {

  /**
   * Construye el comando de imputación analítica territorial a partir de los IDs de unidad
   * organizativa. Resuelve internamente el sourceId, personId, fechas de transferencia y período.
   *
   * @param unitId ID de la unidad organizativa reasignada
   * @param newParentId ID de la nueva unidad padre
   * @return Comando completo con todos los datos necesarios para el prorrateo mid-month
   */
  ImputeAnalyticCommand buildImputeAnalyticCommand(UUID unitId, UUID newParentId);
}
