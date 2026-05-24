package com.solveria.TimeAndBearings.domain.model.enums;

/**
 * Estado del ciclo de vida del AttendanceLedger. Definido en Diccionario de Datos BC-TM v1.2 –
 * AttendanceLedger.status.
 */
public enum LedgerStatus {

  /** Jornada activa; acepta nuevas marcaciones y correcciones. */
  OPEN,

  /**
   * Existen TimeDeviationRecord(s) PENDING que bloquean el cierre. El MSS debe resolver las
   * excepciones antes de transicionar a CLOSED (WF-TM02).
   */
  PENDING_REVIEW,

  /**
   * Ledger cerrado e inmutable. is_finalized=TRUE. No admite modificaciones directas (P-TM33 /
   * Invariante Finalized Record Immutability). Correcciones posteriores al cierre requieren proceso
   * de Reliquidación.
   */
  CLOSED
}
