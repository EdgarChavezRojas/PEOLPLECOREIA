package com.solveria.core.workforce.application.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RelationshipResponse {

  private UUID relationshipId;
  private UUID personId;
  private UUID tenantId;
  private String relationType;
  private String currentStatus;
  private LocalDate hireDate;
  private Instant createdAt;
}
