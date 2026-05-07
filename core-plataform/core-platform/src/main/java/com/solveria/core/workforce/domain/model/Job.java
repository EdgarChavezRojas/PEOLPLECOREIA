package com.solveria.core.workforce.domain.model;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Entity/VO: Job
 *
 * <p>Descriptor del cargo. Atributos estandarizados para muchas posiciones. Se trata como VO o
 * referencia externa (tabla maestra).
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job {

  private UUID jobId;
  private String jobCode;
  private String title;
  private String gradeBand; // Banda salarial asociada
  private String description;

  public static Job create(String jobCode, String title, String gradeBand) {
    if (jobCode == null || jobCode.isBlank() || title == null || title.isBlank()) {
      throw new IllegalArgumentException("jobCode y title son requeridos");
    }

    return Job.builder()
        .jobId(UUID.randomUUID())
        .jobCode(jobCode)
        .title(title)
        .gradeBand(gradeBand)
        .build();
  }
}
