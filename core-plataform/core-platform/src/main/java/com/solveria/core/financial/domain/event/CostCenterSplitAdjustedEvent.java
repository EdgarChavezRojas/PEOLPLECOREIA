package com.solveria.core.financial.domain.event;

import com.solveria.core.financial.domain.model.vo.LaborCostSplit;
import com.solveria.core.shared.events.DomainEvent;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Evento (Sync): Recalcula LaborCostSplit. Invariante 100%. */
public record CostCenterSplitAdjustedEvent(
    UUID sourceId, List<LaborCostSplit> newSplits, Instant occurredAt) implements DomainEvent {

  public CostCenterSplitAdjustedEvent(UUID sourceId, List<LaborCostSplit> newSplits) {
    this(sourceId, newSplits, Instant.now());
  }
}
