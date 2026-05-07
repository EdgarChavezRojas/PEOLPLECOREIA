package com.solveria.TimeAndBearings.domain.exception;

import com.solveria.core.shared.exceptions.DomainException;
import java.util.Map;
import java.util.UUID;

/**
 * Lanzada al intentar modificar directamente cualquier entidad hija de un
 * AttendanceLedger con {@code is_finalized = TRUE}.
 *
 * <p>Enforces: Invariante de Inmutabilidad Post-Cierre (P-TM33).
 * El intento queda registrado en SecurityAuditEntry con actor, timestamp y dato intentado.
 *
 * <p>Correcciones válidas post-cierre deben seguir el proceso de Reliquidación
 * (journal entry de ajuste que no toca los registros originales).
 */
public final class ClosedRecordMutationException extends DomainException {

    private static final String CODE = "TM-DOMAIN-001";

    public ClosedRecordMutationException(UUID ledgerId, String attemptedField) {
        super(CODE,
                Map.of("ledgerId", ledgerId, "field", attemptedField),
                "Attempted direct mutation of finalized AttendanceLedger [" + ledgerId +
                "] on field [" + attemptedField + "]. Use Reliquidacion process instead (P-TM33).");
    }
}
