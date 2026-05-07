package com.solveria.core.experience.infrastructure.adapter;

import com.solveria.core.experience.application.port.out.EventOutboxPort;
import com.solveria.core.experience.application.port.out.PredictionModelPO;
import com.solveria.core.experience.domain.model.PredictionModel;
import com.solveria.core.experience.infrastructure.jpa.PredictionModelJpa;
import com.solveria.core.experience.infrastructure.mapper.PredictionModelMapper;
import com.solveria.core.experience.infrastructure.repository.PredictionModelRepository;
import com.solveria.core.security.context.SecurityTenantContext;
import com.solveria.core.shared.events.DomainEvent;
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
    PredictionModelJpa jpa = mapper.toJpa(model);
    PredictionModelJpa savedJpa = repository.save(jpa);
    PredictionModel saved = mapper.toDomain(savedJpa);

    for (DomainEvent event : events) {
      eventOutboxPort.publish(
          "PredictionModel",
          saved.getModelId(),
          mapper.resolveEventType(event),
          mapper.toEventPayload(saved, event));
    }
  }

  @Override
  public Optional<PredictionModel> findById(UUID modelId) {
    String tenantId = SecurityTenantContext.getCurrentTenantId();
    return repository.findByModelIdAndTenantId(modelId, tenantId).map(mapper::toDomain);
  }
}
