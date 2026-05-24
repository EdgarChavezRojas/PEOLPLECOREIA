package com.solveria.core.experience.infrastructure.jpa;

import com.solveria.core.shared.base.BaseEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA Entity: ApprovalWorkflow. Tabla: experience_approval_workflow. Historial SoD almacenado como
 * JSON (TEXT).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "experience_approval_workflow")
public class ApprovalWorkflowJpa extends BaseEntity {

  @Id
  @Column(name = "workflow_id", nullable = false, updatable = false)
  private UUID workflowId;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "action_id", nullable = false, unique = true)
  private SelfServiceActionJpa selfServiceAction;

  @Column(name = "action_id", insertable = false, updatable = false)
  private UUID actionId;

  @Column(name = "current_step", nullable = false)
  private int currentStep;

  @Column(name = "status", length = 20, nullable = false)
  @Enumerated(EnumType.STRING)
  private com.solveria.core.experience.domain.model.vo.ApprovalStatus status;

  /** Invariante SoD: historial serializado como JSON (Quién/Cuándo). */
  @Column(name = "history", columnDefinition = "TEXT")
  private String history;

}
