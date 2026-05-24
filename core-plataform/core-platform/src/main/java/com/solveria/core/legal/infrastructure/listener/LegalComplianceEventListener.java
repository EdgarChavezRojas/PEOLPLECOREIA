package com.solveria.core.legal.infrastructure.listener;

import com.solveria.core.dossier.domain.event.EligibilitySuspendedByComplianceEvent;
import com.solveria.core.dossier.domain.event.MandatoryComplianceDocMissingEvent;
import com.solveria.core.experience.domain.event.TacitaReconduccionRiskEvent;
import com.solveria.core.financial.domain.event.FundingSourceProjectExhaustedEvent;
import com.solveria.core.legal.application.dto.DraftContractRequest;
import com.solveria.core.legal.application.dto.GenerateContractEvidenceRequest;
import com.solveria.core.legal.application.dto.TerminateContractRequest;
import com.solveria.core.legal.application.port.ContractRepositoryPort;
import com.solveria.core.legal.application.usecase.*;
import com.solveria.core.legal.domain.event.AddendumSalaryAdjustmentApprovedEvent;
import com.solveria.core.legal.domain.event.ContractApprovedEvent;
import com.solveria.core.legal.domain.event.LegalThresholdUpdatedEvent;
import com.solveria.core.legal.domain.model.vo.ContractStatus;
import com.solveria.core.legal.domain.model.vo.ContractType;
import com.solveria.core.legal.infrastructure.jpa.ContractJpa;
import com.solveria.core.shared.events.*;
import com.solveria.core.workforce.domain.event.AcademicProfileRankUpdatedEvent;
import com.solveria.core.workforce.domain.event.OrgUnitGeographicMovedEvent;
import com.solveria.core.workforce.domain.event.RelationshipCreatedEvent;
import com.solveria.core.workforce.domain.event.RelationshipEndedEvent;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LegalComplianceEventListener {

  private final DraftContractUseCase draftContractUseCase;
  private final TerminateContractUseCase terminateContractUseCase;
  private final GenerateContractEvidenceUseCase generateContractEvidenceUseCase;
  private final ProposeContractAddendumUseCase proposeContractAddendumUseCase;
  private final ScanExpiringContractsUseCase scanExpiringContractsUseCase;
  private final ContractEventMapper contractDtoMapper;
  private final ContractRepositoryPort contractRepositoryPort;

  // =========================================================================
  // 1. EVENTOS DESDE BC 1: WORKFORCE & ORG MASTER
  // =========================================================================

  @Transactional
  @EventListener
  public void handle(RelationshipCreatedEvent event) {
    log.info("Recibido RelationshipCreatedEvent para relationshipId: {}", event.relationshipId());
    // Workflow 1: Onboarding Integral - Preparar el borrador del contrato
    // Nota: Asumimos valores por defecto. En un caso real, el evento podría traer más datos
    // o se consultaría al BC 1 para saber el ContractType.
    DraftContractRequest request =
        new DraftContractRequest(
            null, // contractId autogenerado
            event.relationshipId(),
            ContractType.INDEFINIDO,
            null, // employmentCond
            null, // projectId
            event.tenantId());
    draftContractUseCase.execute(request);
  }

  @Transactional
  @EventListener
  public void handle(RelationshipEndedEvent event) {
    log.info("Recibido RelationshipEndedEvent para relationshipId: {}", event.relationshipId());
    // Workflow 9: Offboarding - Terminar el contrato activo
    Optional<ContractJpa> activeContract =
        contractRepositoryPort.findByRelationshipId(event.relationshipId());

    activeContract.ifPresent(
        contract -> {
          TerminateContractRequest request =
              new TerminateContractRequest(
                  contract.getContractId(),
                  contract.getTenantId(),
                  "Terminación automática por fin de vínculo laboral");
          terminateContractUseCase.execute(request);
        });
  }

  @Transactional
  @EventListener
  public void handle(AcademicProfileRankUpdatedEvent event) {
    log.info(
        "Recibido AcademicProfileRankUpdatedEvent para relationshipId: {}. Nuevo rango: {}",
        event.relationshipId(),
        event.newRank());
    Optional<ContractJpa> contractOpt =
        contractRepositoryPort.findByRelationshipId(event.relationshipId());

    if (contractOpt.isPresent() && contractOpt.get().getStatus() == ContractStatus.APPROVED) {
      ContractJpa currentContract = contractOpt.get();

      // Delegamos la complejidad de construir el DTO al Mapper
      var request = contractDtoMapper.toProposeAddendumRequest(currentContract);

      proposeContractAddendumUseCase.execute(request);
      log.info(
          "Adenda por mérito académico propuesta para contrato: {}",
          currentContract.getContractId());
    }
    log.warn(
        "Lógica de generación de Adenda para nuevo rango académico pendiente de mapeo de salarios.");
  }

  @Transactional
  @EventListener
  public void handle(OrgUnitGeographicMovedEvent event) {
    log.info("Recibido OrgUnitGeographicMovedEvent para unitId: {}", event.unitId());
    // Afecta Políticas Regionales (Ej. P9 INFOCAL SCZ).
    // Se debe alertar para revisión de ComplianceSnapshot de los contratos asociados. Pendiente a
    // revision para implementar otra logica
    scanExpiringContractsUseCase.execute();
  }

  // =========================================================================
  // 2. EVENTOS DESDE BC 3: DOSSIER & KARDEX
  // =========================================================================

  @Transactional
  @EventListener
  public void handle(MandatoryComplianceDocMissingEvent event) {
    log.info(
        "Recibido MandatoryComplianceDocMissingEvent para relationshipId: {}",
        event.relationshipId());
    applyComplianceSanction(event.relationshipId());
  }

  @Transactional
  @EventListener
  public void handle(EligibilitySuspendedByComplianceEvent event) {
    log.info(
        "Recibido EligibilitySuspendedByComplianceEvent para relationshipId: {}",
        event.relationshipId());
    applyComplianceSanction(event.relationshipId());
  }

  // Método privado para encapsular la lógica de negocio repetida
  private void applyComplianceSanction(UUID relationshipId) {
    contractRepositoryPort
        .findByRelationshipId(relationshipId)
        .ifPresent(
            contract ->
                terminateContractUseCase.execute(
                    new TerminateContractRequest(
                        contract.getContractId(),
                        contract.getTenantId(),
                        "Terminación/Suspensión por falta de documentos críticos de cumplimiento (Dossier).")));

    log.warn(
        "Trabajador requiere intervención contractual por infracción de Compliance Documental.");
  }

  // =========================================================================
  // 3. EVENTOS DESDE BC 5: FINANCIAL & SOCIAL
  // =========================================================================

  @Transactional
  @EventListener
  public void handle(FundingSourceProjectExhaustedEvent event) {
    log.error(
        "Recibido FundingSourceProjectExhaustedEvent para projectId: {}", event.projectCode());
    // El presupuesto de la ONG llegó a 0.
    // Se deben buscar los contratos asociados a este projectId y emitir alertas de terminación
    // o gatillar directamente una suspensión/terminación.
    // Buscamos todos los contratos atados a este centro de costos / proyecto (ONG)
    // Nota: Asegúrate de tener este método en el ContractRepositoryPort
    List<ContractJpa> affectedContracts = contractRepositoryPort.findByProjectId(event.sourceId());

    for (ContractJpa contract : affectedContracts) {
      if (contract.getStatus() == ContractStatus.APPROVED) {
        TerminateContractRequest request =
            new TerminateContractRequest(
                contract.getContractId(),
                contract.getTenantId(),
                "Terminación forzosa: Agotamiento de fondos de la fuente de financiamiento "
                    + event.projectCode());
        terminateContractUseCase.execute(request);
      }
    }
    log.warn(
        "Se requiere acción manual o automática para contratos bajo el proyecto {}",
        event.projectCode());
  }

  // =========================================================================
  // 4. EVENTOS DESDE BC 6: EXPERIENCE & IA (Predictivo)
  // =========================================================================

  @Transactional
  @EventListener
  public void handle(TacitaReconduccionRiskEvent event) {
    log.warn(
        "Recibido TacitaReconduccionRiskEvent desde la IA para contractId: {} (Faltan {} días). Impacto: {}",
        event.contractId(),
        event.daysUntilExpiry(),
        event.financialImpact());
    // Workflow 3: Renovación Preventiva
    // La IA ya calculó el impacto financiero y los días. BC2 puede marcar explícitamente
    // el contrato con una bandera de riesgo para que no pase desapercibido.
    contractRepositoryPort
        .findById(event.contractId())
        .ifPresent(
            contract -> {
              contract.markTacitaReconduccionRisk();
              contractRepositoryPort.save(contract);
            });
  }

  // =========================================================================
  // 5. EVENTOS INTERNOS (INTRA-BC 2)
  // =========================================================================

  @Transactional
  @EventListener
  public void handle(ContractApprovedEvent event) {
    log.info(
        "Recibido ContractApprovedEvent para contractId: {}. Generando evidencia...",
        event.contractId());
    // Generar Kardex Digital WORM automáticamente tras aprobar el contrato
    contractRepositoryPort
        .findById(event.contractId())
        .ifPresent(
            contract ->
                generateContractEvidenceUseCase.execute(
                    new GenerateContractEvidenceRequest(
                        event.contractId(), contract.getTenantId())));
  }

  @Transactional
  @EventListener
  public void handle(AddendumSalaryAdjustmentApprovedEvent event) {
    log.info(
        "Recibido AddendumSalaryAdjustmentApprovedEvent para addendumId: {}. Generando evidencia...",
        event.addendumId());
    // Generar Kardex Digital WORM automáticamente tras aprobar la adenda
    contractRepositoryPort
        .findById(event.contractId())
        .ifPresent(
            contract ->
                generateContractEvidenceUseCase.execute(
                    new GenerateContractEvidenceRequest(
                        event.contractId(), contract.getTenantId())));
  }

  @Transactional
  @EventListener
  public void handle(LegalThresholdUpdatedEvent event) {
    log.warn(
        "Recibido LegalThresholdUpdatedEvent: Regla {} cambió a {}. Analizando impacto...",
        event.ruleName(),
        event.newValue());
    // Se cambió el Salario Mínimo Nacional u otra regla legal.
    // Aquí se debería lanzar un Job asíncrono que barra todos los contratos activos
    // y proponga adendas automáticamente para aquellos que quedaron por debajo del nuevo piso.
  }
}
