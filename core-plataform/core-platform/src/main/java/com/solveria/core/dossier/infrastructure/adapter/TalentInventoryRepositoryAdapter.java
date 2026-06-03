package com.solveria.core.dossier.infrastructure.adapter;

import com.solveria.core.dossier.application.port.TalentInventoryRepositoryPort;
import com.solveria.core.dossier.domain.model.TalentInventory;
import com.solveria.core.dossier.infrastructure.jpa.TalentInventoryJpa;
import com.solveria.core.dossier.infrastructure.mapper.TalentInventoryMapper;
import com.solveria.core.dossier.infrastructure.repository.TalentInventoryRepository;
import com.solveria.core.security.context.SecurityTenantContext;
import com.solveria.core.shared.outbox.application.port.EventOutboxPort;
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
    // Idempotency: one TalentInventory per relationship — upsert to avoid duplicates on retries
    TalentInventoryJpa jpa =
        talentInventoryRepository
            .findByRelationshipIdAndTenantId(inventory.getRelationshipId(), inventory.getTenantId())
            .map(
                existing -> {
                  // Replace entire JPA content with latest domain state
                  TalentInventoryJpa updated = talentInventoryMapper.toJpa(inventory);
                  updated.setInventoryId(existing.getInventoryId()); // keep original PK
                  return updated;
                })
            .orElseGet(() -> talentInventoryMapper.toJpa(inventory));

    TalentInventoryJpa savedJpa = talentInventoryRepository.save(jpa);
    TalentInventory saved = talentInventoryMapper.toDomain(savedJpa);

    eventOutboxPort.publish(inventory.pullDomainEvents());

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
