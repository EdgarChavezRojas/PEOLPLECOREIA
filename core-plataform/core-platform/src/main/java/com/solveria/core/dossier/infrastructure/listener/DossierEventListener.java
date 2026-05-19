// Ruta: core-plataform/core-platform/src/main/java/com/solveria/core/dossier/infrastructure/listener/DossierEventListener.java
package com.solveria.core.dossier.infrastructure.listener;

import com.solveria.core.dossier.application.command.ArchiveContractCommand;
import com.solveria.core.dossier.application.command.CreateDocumentRequirementsCommand;
import com.solveria.core.dossier.application.command.RecordDisciplinaryActionCommand;
import com.solveria.core.dossier.application.command.ReturnAssetCommand;
import com.solveria.core.dossier.application.port.AssignedAssetRepositoryPort;
import com.solveria.core.dossier.application.usecase.ArchiveContractUseCase;
import com.solveria.core.dossier.application.usecase.CreateDocumentRequirementsUseCase;
import com.solveria.core.dossier.application.usecase.RecordDisciplinaryActionUseCase;
import com.solveria.core.dossier.application.usecase.ReturnAssetUseCase;
import com.solveria.core.dossier.domain.model.vo.DisciplinarySeverity;
import com.solveria.core.dossier.domain.policy.LocalizationPolicy;
import com.solveria.core.experience.domain.event.DisciplinaryThresholdReachedEvent;

import com.solveria.core.legal.domain.event.ContractApprovedEvent;
import com.solveria.core.workforce.domain.event.OrgUnitGeographicMovedEvent;
import com.solveria.core.workforce.domain.event.PersonMasterCreatedEvent;
import com.solveria.core.workforce.domain.event.PersonUpdatedEvent;
import com.solveria.core.workforce.domain.event.RelationshipEndedEvent;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DossierEventListener {

  private static final String CONTRACT_REFERENCE_DEFAULT = "EVIDENCIA_CONTRATO_WORM";

  private final CreateDocumentRequirementsUseCase createDocumentRequirementsUseCase;
  private final ArchiveContractUseCase archiveContractUseCase;
  private final ReturnAssetUseCase returnAssetUseCase;
  private final RecordDisciplinaryActionUseCase recordDisciplinaryActionUseCase;
  //private final AcknowledgeMemorandumUseCase acknowledgeMemorandumUseCase;
  private final AssignedAssetRepositoryPort assignedAssetRepository;

  @EventListener
  @Transactional
  public void handle(PersonMasterCreatedEvent event) {
    log.info("Recibido PersonMasterCreatedEvent para personId={}", event.personId());
    CreateDocumentRequirementsCommand command =
        new CreateDocumentRequirementsCommand(
            event.personId(), event.tenantId());
    createDocumentRequirementsUseCase.handle(command);
  }

  @EventListener
  @Transactional
  public void handle(ContractApprovedEvent event) {
    log.info("Recibido ContractApprovedEvent para contractId={}", event.contractId());
    ArchiveContractCommand command =
        new ArchiveContractCommand(
            event.contractId(),
            event.contractId(),
            CONTRACT_REFERENCE_DEFAULT,
            event.tenantId());
    archiveContractUseCase.handle(command);
  }

  @EventListener
  @Transactional
  public void handle(RelationshipEndedEvent event) {
    log.info("Recibido RelationshipEndedEvent para relationshipId={}", event.relationshipId());

    // Necesitas inyectar AssignedAssetRepositoryPort en el DossierEventListener
    List<UUID> pendingAssetIds = assignedAssetRepository.findPendingAssetIds(event.relationshipId());

    for (UUID assetId : pendingAssetIds) {
      ReturnAssetCommand command = new ReturnAssetCommand(
              assetId, // <-- ID correcto del activo
              LocalDateTime.now(),
              false,
              LocalizationPolicy.SANTA_CRUZ_BOLIVIA,
              event.tenantId());
      returnAssetUseCase.handle(command);
    }
  }

  @EventListener
  @Transactional
  public void handle(DisciplinaryThresholdReachedEvent event) {
    log.info("Recibido DisciplinaryThresholdReachedEvent para tenantId={}", event.tenantId());
    String reason = "AI_THRESHOLD_REACHED memos=%d periodMonths=%d recommendation=%s"
            .formatted(event.memorandumCount(), event.periodMonths(), event.recommendation());

    RecordDisciplinaryActionCommand command =
        new RecordDisciplinaryActionCommand(
            event.relationshipId(),
            DisciplinarySeverity.CRITICAL,
            reason,
            reason.getBytes(StandardCharsets.UTF_8),
            "ai_disciplinary_alert.txt",
            LocalDate.now().plusYears(1),
                LocalizationPolicy.SANTA_CRUZ_BOLIVIA,
            event.tenantId());
    recordDisciplinaryActionUseCase.handle(command);
  }

//  @EventListener revisar este evento porque el flujo no es el correcto
//  @Transactional
//  public void handle(MemorandumAcknowledgedEvent event) {
//    log.info("Procesando acuse de recibo para notificación: {}", event.notificationId());
//
//    // 1. RESOLVER LA UBICACIÓN (Ya que el BC6 no la envía)
//    // Opción A: Si tu BC3 tiene forma de consultar la ciudad del empleado:
//    // LocalizationPolicy policy = workforceQueryPort.getPolicyForPerson(event.personId());
//
//    // Opción B (Fallback provisional si aún no tienes el puerto):
//    LocalizationPolicy policy = LocalizationPolicy.SANTA_CRUZ_BOLIVIA;
//
//    // 2. RESOLVER LA FIRMA (Generar un rastro de auditoría determinista y seguro)
//    // Esto es mucho más profesional y auditable que usar "ACK" o datos falsos.
//    String auditTrail = String.format("SYSTEM_E_SIGNATURE|NOTIF:%s|PERSON:%s|TIMESTAMP:%s|TENANT:%s",
//            event.notificationId(),
//            event.personId(),
//            event.acknowledgedAt(),
//            event.tenantId());
//    byte[] simpleElectronicSignature = auditTrail.getBytes(StandardCharsets.UTF_8);
//
//    // 3. ARMAR EL COMANDO PARA EL DOSSIER
//    AcknowledgeMemorandumCommand command = new AcknowledgeMemorandumCommand(
//            // Nota: Asegúrate de que tu BC3 pueda buscar el documentId a partir del notificationId
//            event.notificationId(),
//            simpleElectronicSignature, // La firma autogenerada por auditoría
//            null,                      // Expiración (null = permanente o calculada en el Service)
//            policy,                    // La política resuelta
//            UUID.fromString(event.tenantId())
//    );
//
//    // 4. EJECUTAR EL CASO DE USO
//    acknowledgeMemorandumUseCase.handle(command);
//  }
  @Async
  @EventListener
  public void handle(PersonUpdatedEvent event) {
    log.info("Recibido PersonUpdatedEvent para personId={}. Pendiente de procesamiento.", event.personId());
  }
  @Async
  @EventListener
  public void handle(OrgUnitGeographicMovedEvent event) {
    log.info("Recibido OrgUnitGeographicMovedEvent para unitId={}. Pendiente de procesamiento.", event.unitId());
  }
}

