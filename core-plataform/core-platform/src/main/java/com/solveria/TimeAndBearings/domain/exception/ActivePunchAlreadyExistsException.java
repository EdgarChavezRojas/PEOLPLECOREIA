package com.solveria.TimeAndBearings.domain.exception;

import com.solveria.core.shared.exceptions.DomainException;
import java.util.Map;
import java.util.UUID;

/**
 * Lanzada cuando se intenta registrar un segundo PUNCH_IN activo en un
 * AttendanceLedger que ya tiene un PUNCH_IN sin su PUNCH_OUT correspondiente.
 *
 * <p>Enforces: Invariante de Singularidad de Punch Activo (Active Punch Uniqueness).
 * Un colaborador no puede estar "adentro" dos veces al mismo tiempo.
 */
public final class ActivePunchAlreadyExistsException extends DomainException {

    private static final String CODE = "TM-DOMAIN-002";

    public ActivePunchAlreadyExistsException(UUID ledgerId, UUID relationshipId) {
        super(CODE,
                Map.of("ledgerId", ledgerId, "relationshipId", relationshipId),
                "Relationship [" + relationshipId + "] already has an active PUNCH_IN without" +
                " a corresponding PUNCH_OUT on ledger [" + ledgerId + "] (Active Punch Uniqueness invariant).");
    }
}
