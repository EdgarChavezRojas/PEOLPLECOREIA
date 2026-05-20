package com.solveria.core.experience.infrastructure.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solveria.core.experience.domain.event.*;
import com.solveria.core.experience.domain.model.ApprovalWorkflow;
import com.solveria.core.experience.domain.model.SelfServiceAction;
import com.solveria.core.experience.domain.model.vo.CertificatePayload;
import com.solveria.core.experience.infrastructure.jpa.ApprovalWorkflowJpa;
import com.solveria.core.experience.infrastructure.jpa.SelfServiceActionJpa;
import com.solveria.core.shared.events.DomainEvent;
import java.util.List;
import java.util.Map;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SelfServiceActionMapper {

  @Mapping(target = "approvalWorkflow", ignore = true)
  SelfServiceActionJpa toJpa(SelfServiceAction action);

  @AfterMapping
  default void enrichJpa(@MappingTarget SelfServiceActionJpa jpa, SelfServiceAction action) {
    if (jpa == null) return;
    jpa.setTenantId(action.getTenantId());

    // Map certificate payload fields
    CertificatePayload cert = action.getCertificatePayload();
    if (cert != null) {
      jpa.setCertType(cert.certificateType());
      jpa.setCertPdfContent(cert.pdfBase64Content());
      jpa.setCertSha256Hash(cert.sha256Hash());
      jpa.setCertQrUrl(cert.qrValidationUrl());
      jpa.setCertGeneratedAt(cert.generatedAt());
    }

    // Map approval workflow
    ApprovalWorkflow wf = action.getApprovalWorkflow();
    if (wf != null) {
      ApprovalWorkflowJpa wfJpa = new ApprovalWorkflowJpa();
      wfJpa.setWorkflowId(wf.getWorkflowId());
      wfJpa.setSelfServiceAction(jpa);
      wfJpa.setCurrentStep(wf.getCurrentStep());
      wfJpa.setStatus(wf.getStatus());
      wfJpa.setCreatedAt(wf.getCreatedAt());
      try {
        wfJpa.setHistory(new ObjectMapper().writeValueAsString(wf.getHistory()));
      } catch (Exception e) {
        wfJpa.setHistory("[]");
      }
      jpa.setApprovalWorkflow(wfJpa);
    }
  }

  default SelfServiceAction toDomain(SelfServiceActionJpa jpa) {
    if (jpa == null) return null;

    CertificatePayload cert = null;
    if (jpa.getCertType() != null && jpa.getCertSha256Hash() != null) {
      cert =
          new CertificatePayload(
              jpa.getCertType(),
              jpa.getCertPdfContent(),
              jpa.getCertSha256Hash(),
              jpa.getCertQrUrl(),
              jpa.getCertGeneratedAt());
    }

    ApprovalWorkflow wf = null;
    ApprovalWorkflowJpa wfJpa = jpa.getApprovalWorkflow();
    if (wfJpa != null) {
      List<ApprovalWorkflow.ApprovalHistoryEntry> historyEntries;
      try {
        historyEntries =
            new ObjectMapper()
                .readValue(
                    wfJpa.getHistory(),
                    new TypeReference<List<ApprovalWorkflow.ApprovalHistoryEntry>>() {});
      } catch (Exception e) {
        historyEntries = List.of();
      }
      wf =
          ApprovalWorkflow.rehydrate(
              wfJpa.getWorkflowId(),
              jpa.getActionId(),
              wfJpa.getCurrentStep(),
              wfJpa.getStatus(),
              historyEntries,
              wfJpa.getCreatedAt());
    }

    return SelfServiceAction.rehydrate(
        jpa.getActionId(),
        jpa.getPersonId(),
        jpa.getActionType(),
        jpa.getPayload(),
        jpa.getTenantId(),
        jpa.getCreatedBy(),
        jpa.getCreatedAt(),
        wf,
        cert);
  }

  default String toEventPayload(SelfServiceAction action, DomainEvent event) {
    if (action == null || event == null) return "{}";
    Map<String, Object> payload =
        Map.of(
            "actionId", action.getActionId(),
            "personId", action.getPersonId(),
            "tenantId", action.getTenantId(),
            "actionType", action.getActionType().name(),
            "eventType", resolveEventType(event));
    try {
      return new ObjectMapper().writeValueAsString(payload);
    } catch (Exception e) {
      throw new RuntimeException("Error serializando SelfServiceAction a JSON", e);
    }
  }

  default String resolveEventType(DomainEvent event) {
    if (event instanceof DataChangeRequestedEvent) return "DATA_CHANGE_REQUESTED";
    if (event instanceof DataChangeRejectedEvent) return "DATA_CHANGE_REJECTED";
    if (event instanceof CertificateGeneratedEvent) return "CERTIFICATE_GENERATED";
    if (event instanceof NotificationSentEvent) return "NOTIFICATION_SENT";
    return event.getClass().getSimpleName();
  }
}
