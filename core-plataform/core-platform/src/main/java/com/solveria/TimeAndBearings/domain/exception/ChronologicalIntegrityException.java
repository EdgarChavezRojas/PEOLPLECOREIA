package com.solveria.TimeAndBearings.domain.exception;

import com.solveria.core.shared.exceptions.DomainException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Lanzada cuando un punch_time viola la secuencia cronológica del Ledger.
 *
 * <p>Enforces: Invariante de Secuencia Cronológica y Causalidad (Chronological Integrity):
 * <ul>
 *   <li>Regla A: PUNCH_OUT debe ser estrictamente posterior al PUNCH_IN.</li>
 *   <li>Regla B: Ningún TimeEntry puede tener punch_time en el futuro (tolerancia ±5s, P-TM26).</li>
 * </ul>
 */
public final class ChronologicalIntegrityException extends DomainException {

    private static final String CODE = "TM-DOMAIN-003";

    public ChronologicalIntegrityException(UUID ledgerId, LocalDateTime violatingPunchTime, String rule) {
        super(CODE,
                Map.of("ledgerId", ledgerId, "violatingPunchTime", violatingPunchTime, "rule", rule),
                "Chronological integrity violated on ledger [" + ledgerId + "]: punch_time=" +
                violatingPunchTime + " – " + rule);
    }
}
