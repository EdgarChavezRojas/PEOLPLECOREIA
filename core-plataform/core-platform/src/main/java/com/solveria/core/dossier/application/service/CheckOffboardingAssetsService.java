package com.solveria.core.dossier.application.service;

import com.solveria.core.dossier.application.command.CheckOffboardingAssetsCommand;
import com.solveria.core.dossier.application.port.AssignedAssetRepositoryPort;
import com.solveria.core.dossier.application.usecase.CheckOffboardingAssetsUseCase;
import com.solveria.core.dossier.domain.event.OffboardingBlockedByAssetsEvent;
import com.solveria.core.dossier.domain.policy.LocalizationPolicy;
import com.solveria.core.shared.outbox.application.port.EventOutboxPort;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CheckOffboardingAssetsService implements CheckOffboardingAssetsUseCase {

  private final AssignedAssetRepositoryPort assignedAssetRepository;
  private final EventOutboxPort eventOutboxPort;

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

    // Publicamos directamente usando el nuevo contrato del puerto
    eventOutboxPort.publish(List.of(enrichedEvent));
  }
}
