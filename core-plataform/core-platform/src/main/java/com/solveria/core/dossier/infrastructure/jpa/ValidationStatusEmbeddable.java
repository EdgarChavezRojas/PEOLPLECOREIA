package com.solveria.core.dossier.infrastructure.jpa;

import com.solveria.core.dossier.domain.model.vo.ValidationState;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class ValidationStatusEmbeddable {

  @Enumerated(EnumType.STRING)
  @Column(name = "current_state", nullable = false)
  private ValidationState currentState;

  @Column(name = "reviewer_id")
  private UUID reviewerId;

  @Column(name = "review_date")
  private LocalDateTime reviewDate;

  @Column(name = "reject_reason")
  private String rejectReason;
}
