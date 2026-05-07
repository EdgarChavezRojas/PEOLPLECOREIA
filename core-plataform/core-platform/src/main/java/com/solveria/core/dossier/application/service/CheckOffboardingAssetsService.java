package com.solveria.core.dossier.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solveria.core.dossier.application.command.CheckOffboardingAssetsCommand;
import com.solveria.core.dossier.application.port.AssignedAssetRepositoryPort;
import com.solveria.core.dossier.application.usecase.CheckOffboardingAssetsUseCase;
import com.solveria.core.dossier.domain.event.DossierEventType;
import com.solveria.core.dossier.domain.event.OffboardingBlockedByAssetsEvent;
import com.solveria.core.dossier.domain.policy.LocalizationPolicy;
import com.solveria.core.workforce.application.port.EventOutboxPort;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CheckOffboardingAssetsService implements CheckOffboardingAssetsUseCase {

  private final AssignedAssetRepositoryPort assignedAssetRepository;
  private final EventOutboxPort eventOutboxPort;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public CheckOffboardingAssetsService(
      AssignedAssetRepositoryPort assignedAssetRepository, EventOutboxPort eventOutboxPort) {
    this.assignedAssetRepository = assignedAssetRepository;
    this.eventOutboxPort = eventOutboxPort;
  }

  @Override
  public boolean handle(CheckOffboardingAssetsCommand command) {
    LocalizationPolicy.requireSantaCruz(command.location());
    boolean blocked = assignedAssetRepository.hasPendingAssets(command.workerId());
    if (blocked) {
      publishBlockedEvent(command);
    }
    return blocked;
  }

  private void publishBlockedEvent(CheckOffboardingAssetsCommand command) {
    List<UUID> unreturnedIds = assignedAssetRepository.findPendingAssetIds(command.workerId());
    OffboardingBlockedByAssetsEvent enrichedEvent =
        OffboardingBlockedByAssetsEvent.now(command.workerId(), unreturnedIds);

    String payload;
    try {
      payload =
          objectMapper.writeValueAsString(
              Map.of(
                  "personId", enrichedEvent.personId(),
                  "unreturnedAssetIds", enrichedEvent.unreturnedAssetIds(),
                  "occurredAt", enrichedEvent.occurredAt().toString(),
                  "tenantId", command.tenantId(),
                  "eventType", DossierEventType.OFFBOARDING_BLOCKED_BY_ASSETS.name()));
    } catch (Exception e) {
      payload = "{}";
    }
    eventOutboxPort.publish(
        "AssignedAsset",
        command.workerId(),
        DossierEventType.OFFBOARDING_BLOCKED_BY_ASSETS.name(),
        payload);
  }
}
