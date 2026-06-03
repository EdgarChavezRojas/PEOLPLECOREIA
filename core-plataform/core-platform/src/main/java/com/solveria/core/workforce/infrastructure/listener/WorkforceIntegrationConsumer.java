package com.solveria.core.workforce.infrastructure.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solveria.core.dossier.domain.event.DocentAcademicTitleVerifiedEvent;
import com.solveria.core.experience.domain.event.DataChangeRequestedEvent;
import com.solveria.core.legal.application.port.ContractRepositoryPort;
import com.solveria.core.legal.domain.event.ContractApprovedEvent;
import com.solveria.core.legal.domain.event.ContractTerminatedEvent;
import com.solveria.core.security.context.SecurityTenantContext;
import com.solveria.core.workforce.application.usecase.ReactivateRelationshipUseCase;
import com.solveria.core.workforce.application.usecase.TerminateRelationshipUseCase;
import com.solveria.core.workforce.application.usecase.UpdateEmployeeAcademicProfileUseCase;
import com.solveria.core.workforce.application.usecase.UpdatePersonUseCase;
import com.solveria.core.workforce.domain.model.vo.ContactPoint;
import com.solveria.core.workforce.domain.model.vo.MaritalStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkforceIntegrationConsumer {

  // Casos de uso existentes inyectados
  private final UpdateEmployeeAcademicProfileUseCase updateAcademicRankUseCase;
  private final UpdatePersonUseCase updatePersonUseCase;
  private final ReactivateRelationshipUseCase reactivateRelationshipUseCase;
  private final TerminateRelationshipUseCase terminateRelationshipUseCase;
  private final ContractRepositoryPort contractRepositoryPort;
  private final ObjectMapper objectMapper;

  @EventListener
  @Transactional
  public void handle(DocentAcademicTitleVerifiedEvent event) {
    log.info(
        "Procesando evento DOCENT_ACADEMIC_TITLE_VERIFIED para relación: {}",
        event.relationshipId());

    // 1. Obtenemos el tenantId a partir del contexto actual de seguridad o del evento (si decides
    // agregarlo)
    // Dado que tu caso de uso pide tenantId, lo ideal es recuperarlo directamente de la relación o
    // del contexto.
    // corregir esto a futuro porque sino se genera un  null pointer exception
    UUID currentTenantId = UUID.fromString(SecurityTenantContext.getCurrentTenantId());

    // 2. Ejecutamos el caso de uso existente directamente usando el relationshipId del evento
    updateAcademicRankUseCase.execute(event.relationshipId(), currentTenantId, event.titleLevel());
  }

  @EventListener
  @Transactional
  public void handle(DataChangeRequestedEvent event) {
    log.info(
        "Procesando evento DATA_CHANGE_REQUESTED (Acción {}) para persona: {}",
        event.actionId(),
        event.personId());
    try {
      // El listener traduce la infraestructura (JSON) al dominio
      JsonNode payload = objectMapper.readTree(event.payload());

      MaritalStatus maritalStatus =
          payload.has("maritalStatus")
              ? MaritalStatus.valueOf(payload.get("maritalStatus").asText().toUpperCase())
              : null;

      String professionTitle =
          payload.has("professionTitle") ? payload.get("professionTitle").asText() : null;

      // Asumimos lista vacía por ahora. Podrías mapear JsonNode a ContactPoint si viene en el JSON
      List<ContactPoint> contacts = new ArrayList<>();

      // Ejecutamos el caso de uso existente
      updatePersonUseCase.execute(event.personId(), maritalStatus, professionTitle, contacts);

    } catch (Exception e) {
      log.error("Fallo al parsear payload del evento: {}", event.actionId(), e);
      throw new IllegalArgumentException("Payload JSON inválido en evento de cambio de datos", e);
    }
  }

  @EventListener
  @Transactional
  public void handle(ContractApprovedEvent event) {
    log.info(
        "Procesando evento CONTRACT_APPROVED. Reactivando relación para contrato: {}",
        event.contractId());
    UUID relationshipId =
        contractRepositoryPort
            .findById(event.contractId())
            .map(com.solveria.core.legal.domain.model.Contract::getRelationshipId)
            .orElse(event.contractId());
    reactivateRelationshipUseCase.execute(relationshipId, event.tenantId());
  }

  @EventListener
  @Transactional
  public void handle(ContractTerminatedEvent event) {
    log.info(
        "Procesando evento CONTRACT_TERMINATED. Terminando relación para contrato: {}",
        event.contractId());
    UUID relationshipId =
        contractRepositoryPort
            .findById(event.contractId())
            .map(com.solveria.core.legal.domain.model.Contract::getRelationshipId)
            .orElse(event.contractId());
    String reason = "Finalización de contrato confirmada desde evento Legal/Compliance";
    terminateRelationshipUseCase.execute(relationshipId, reason, event.tenantId());
  }
}
