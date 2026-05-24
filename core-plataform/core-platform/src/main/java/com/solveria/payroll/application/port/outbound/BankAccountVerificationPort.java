package com.solveria.payroll.application.port.outbound;
import java.util.UUID;

/**
 * Puerto de Salida Secundario (Outbound Port).
 * Abstracción pura de dominio para verificar el estado de validación bancaria de un empleado.
 * Reemplaza de forma definitiva el uso de espionaje técnico sobre la tabla Outbox.
 */
public interface BankAccountVerificationPort {

    /**
     * Verifica si el empleado posee una cuenta bancaria legítima, activa y sincronizada
     * en el Core HR para el procesamiento seguro de fondos.
     *
     * @param employeeId ID del colaborador (relationshipId)
     * @param tenantId   ID de la organización/inquilino
     * @return true si la cuenta cumple con las invariantes operacionales del negocio
     */
    boolean isBankAccountValidated(UUID employeeId, UUID tenantId);
}