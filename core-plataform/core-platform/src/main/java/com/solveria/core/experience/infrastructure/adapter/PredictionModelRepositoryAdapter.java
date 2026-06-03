package com.solveria.core.experience.infrastructure.adapter;

import com.solveria.core.experience.application.port.out.PredictionModelPO;
import com.solveria.core.experience.domain.model.PredictionModel;
import com.solveria.core.experience.infrastructure.jpa.PredictionModelJpa;
import com.solveria.core.experience.infrastructure.mapper.PredictionModelMapper;
import com.solveria.core.experience.infrastructure.repository.PredictionModelRepository;
import com.solveria.core.security.context.SecurityTenantContext;
import com.solveria.core.shared.events.DomainEvent;
import com.solveria.core.shared.outbox.application.port.EventOutboxPort;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Adapter: PredictionModelPO. Persiste el agregado PredictionModel y publica eventos al outbox. */
@Slf4j
@Component
@RequiredArgsConstructor
public class PredictionModelRepositoryAdapter implements PredictionModelPO {

  private final PredictionModelRepository repository;
  private final PredictionModelMapper mapper;
  private final EventOutboxPort eventOutboxPort;

  @Override
  @Transactional
  public void save(PredictionModel model) {
    List<DomainEvent> events = model.pullDomainEvents();
    // Idempotency: one PredictionModel per tenant — upsert by modelId + tenantId
    PredictionModelJpa jpa =
        repository
            .findByModelIdAndTenantId(model.getModelId(), model.getTenantId())
            .map(
                existing -> {
                  existing.setModelType(model.getModelType());
                  existing.setVersion(model.getVersion());
                  existing.setLastExecution(model.getLastExecution());
                  try {
                    existing.setAlerts(
                        new com.fasterxml.jackson.databind.ObjectMapper()
                            .writeValueAsString(model.getAlerts()));
                  } catch (Exception e) {
                    existing.setAlerts("[]");
                  }
                  return existing;
                })
            .orElseGet(() -> mapper.toJpa(model));

    repository.save(jpa);
    eventOutboxPort.publish(events);
  }

  @Override
  public Optional<PredictionModel> findById(UUID modelId) {
    UUID tenantId = UUID.fromString(SecurityTenantContext.getCurrentTenantId());
    return repository.findByModelIdAndTenantId(modelId, tenantId).map(mapper::toDomain);
  }

  @Override
  public Optional<PredictionModel> findByTenantId(UUID tenantId) {
    return repository.findByTenantId(tenantId).map(mapper::toDomain);
  }
}
