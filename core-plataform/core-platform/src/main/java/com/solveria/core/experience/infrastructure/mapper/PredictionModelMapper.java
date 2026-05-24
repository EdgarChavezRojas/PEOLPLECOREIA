package com.solveria.core.experience.infrastructure.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solveria.core.experience.domain.event.*;
import com.solveria.core.experience.domain.model.PredictionModel;
import com.solveria.core.experience.domain.model.vo.RiskAlert;
import com.solveria.core.experience.infrastructure.jpa.PredictionModelJpa;
import com.solveria.core.shared.events.DomainEvent;
import java.util.List;
import java.util.Map;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PredictionModelMapper {

  default PredictionModelJpa toJpa(PredictionModel model) {
    if (model == null) return null;
    PredictionModelJpa jpa = new PredictionModelJpa();
    jpa.setModelId(model.getModelId());
    jpa.setModelType(model.getModelType());
    jpa.setVersion(model.getVersion());
    jpa.setLastExecution(model.getLastExecution());
    jpa.setTenantId(model.getTenantId());
    try {
      jpa.setAlerts(new ObjectMapper().writeValueAsString(model.getAlerts()));
    } catch (Exception e) {
      jpa.setAlerts("[]");
    }
    return jpa;
  }

  default PredictionModel toDomain(PredictionModelJpa jpa) {
    if (jpa == null) return null;
    List<RiskAlert> alerts;
    try {
      alerts =
          new ObjectMapper().readValue(jpa.getAlerts(), new TypeReference<List<RiskAlert>>() {});
    } catch (Exception e) {
      alerts = List.of();
    }
    return PredictionModel.rehydrate(
        jpa.getModelId(),
        jpa.getModelType(),
        jpa.getVersion(),
        jpa.getLastExecution(),
        jpa.getTenantId(),
        alerts);
  }

  default String toEventPayload(PredictionModel model, DomainEvent event) {
    if (model == null || event == null) return "{}";
    Map<String, Object> payload =
        Map.of(
            "modelId", model.getModelId(),
            "modelType", model.getModelType().name(),
            "tenantId", model.getTenantId(),
            "eventType", resolveEventType(event));
    try {
      return new ObjectMapper().writeValueAsString(payload);
    } catch (Exception e) {
      throw new RuntimeException("Error serializando PredictionModel a JSON", e);
    }
  }

  default String resolveEventType(DomainEvent event) {
    if (event instanceof TacitaReconduccionRiskEvent) return "CONTRACT_TACITA_RECONDUCCION_RISK";
    if (event instanceof DisciplinaryThresholdReachedEvent) return "DISCIPLINARY_THRESHOLD_REACHED";
    return event.getClass().getSimpleName();
  }
}
