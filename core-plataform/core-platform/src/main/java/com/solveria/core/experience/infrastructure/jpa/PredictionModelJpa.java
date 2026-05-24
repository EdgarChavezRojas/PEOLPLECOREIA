package com.solveria.core.experience.infrastructure.jpa;

import com.solveria.core.shared.base.BaseEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA Entity: PredictionModel. Tabla: experience_prediction_model. Las alertas se almacenan como
 * JSON TEXT (desnormalizado).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "experience_prediction_model")
public class PredictionModelJpa extends BaseEntity {

  @Id
  @Column(name = "model_id", nullable = false, updatable = false)
  private UUID modelId;

  @Column(name = "model_type", length = 30, nullable = false)
  @Enumerated(EnumType.STRING)
  private com.solveria.core.experience.domain.model.vo.ModelType modelType;



  @Column(name = "last_execution")
  private Instant lastExecution;


  /** RiskAlerts serializadas como JSON array. */
  @Column(name = "alerts", columnDefinition = "TEXT")
  private String alerts;
}
