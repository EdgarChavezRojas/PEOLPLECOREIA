package com.solveria.core.dossier.application.port;

import com.solveria.core.dossier.domain.model.TalentInventory;
import java.util.Optional;
import java.util.UUID;

public interface TalentInventoryRepositoryPort {

  TalentInventory save(TalentInventory inventory);

  Optional<TalentInventory> findByRelationshipId(UUID relationshipId);
}
