package com.solveria.core.dossier.infrastructure.adapter;

import com.solveria.core.dossier.application.port.TalentInventoryRepositoryPort;
import com.solveria.core.dossier.domain.event.DossierEvent;
import com.solveria.core.dossier.domain.model.TalentInventory;
import com.solveria.core.dossier.infrastructure.jpa.TalentInventoryJpa;
import com.solveria.core.dossier.infrastructure.mapper.TalentInventoryMapper;
import com.solveria.core.dossier.infrastructure.repository.TalentInventoryRepository;
import com.solveria.core.security.context.SecurityTenantContext;
import com.solveria.core.shared.events.DomainEvent;
import com.solveria.core.workforce.application.port.EventOutboxPort;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class TalentInventoryRepositoryAdapter implements TalentInventoryRepositoryPort {

  private final TalentInventoryRepository talentInventoryRepository;
  private final TalentInventoryMapper talentInventoryMapper;
  private final EventOutboxPort eventOutboxPort;

  @Override
  @Transactional
  public TalentInventory save(TalentInventory inventory) {
    List<DomainEvent> events = inventory.pullDomainEvents();
    TalentInventoryJpa jpa = talentInventoryMapper.toJpa(inventory);
    TalentInventoryJpa savedJpa = talentInventoryRepository.save(jpa);
    TalentInventory saved = talentInventoryMapper.toDomain(savedJpa);

    for (DomainEvent event : events) {
      if (event instanceof DossierEvent dossierEvent) {
        eventOutboxPort.publish(
            "TalentInventory",
            saved.getInventoryId(),
            dossierEvent.type().name(),
            talentInventoryMapper.toEventPayload(saved, dossierEvent));
      }
    }

    return saved;
  }

  @Override
  public Optional<TalentInventory> findByRelationshipId(UUID relationshipId) {
    String tenantId = SecurityTenantContext.getCurrentTenantId();
    return talentInventoryRepository
        .findByRelationshipIdAndTenantId(relationshipId, UUID.fromString(tenantId))
        .map(talentInventoryMapper::toDomain);
  }
}
