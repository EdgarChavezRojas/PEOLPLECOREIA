package com.solveria.core.financial.application.port;

import com.solveria.core.financial.application.command.ProcessLiquidationCommand;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Puerto secundario (outbound): Resuelve datos financieros de contratos desde otros BCs.
 *
 * <p>Anti-Corruption Layer que permite al Financial BC obtener datos que los eventos cross-BC no
 * transportan (e.g., salarios, tipo de terminación, datos de presupuesto). Evita que el listener
 * fabrique o hardcodee datos que no le pertenecen.
 */
public interface ContractFinancialDataPort {

  /**
   * Construye el comando de liquidación completo enriqueciendo los datos del contrato con
   * información salarial, de vacaciones y de tipo de terminación resueltos desde el BC de origen.
   *
   * @param contractId ID del contrato terminado
   * @return Comando de liquidación con todos los datos necesarios
   */
  ProcessLiquidationCommand buildLiquidationCommand(UUID contractId);

  /**
   * Obtiene el monto presupuestario asociado a un contrato borrador para la reserva preventiva
   * (pre-encumbrance).
   *
   * @param contractId ID del contrato borrador
   * @return Monto requerido para la reserva
   */
  BigDecimal getRequiredBudgetForContract(UUID contractId);

  /**
   * Obtiene el monto a asignar formalmente al aprobar un contrato.
   *
   * @param contractId ID del contrato aprobado
   * @return Monto a formalizar en la asignación presupuestaria
   */
  BigDecimal getAllocationAmountForContract(UUID contractId);

  /**
   * Obtiene el ID de la fuente de financiamiento asociada al contrato.
   *
   * @param contractId ID del contrato
   * @return UUID de la fuente de financiamiento
   */
  UUID getFundingSourceIdForContract(UUID contractId);

  /**
   * Obtiene el usuario aprobador asociado al contrato para validaciones de SoD.
   *
   * @param contractId ID del contrato
   * @return ID del usuario aprobador
   */
  String getApproverForContract(UUID contractId);
}
