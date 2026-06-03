package com.solveria.core.experience.application.port.out;

import com.solveria.core.experience.domain.model.PredictionModel;
import java.util.Optional;
import java.util.UUID;

/** Secondary Port (Outbound): Repositorio de PredictionModel. */
public interface PredictionModelPO {

  void save(PredictionModel model);

  Optional<PredictionModel> findById(UUID modelId);

  Optional<PredictionModel> findByTenantId(UUID tenantId);
}
