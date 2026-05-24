package com.solveria.core.workforce.domain.model;

import java.util.UUID;

/**
 * Entity/VO: Job
 *
 * <p>Descriptor del cargo. Atributos estandarizados para muchas posiciones. Se trata como VO o
 * referencia externa (tabla maestra).
 */
public class Job {

  private UUID jobId;
  private String jobCode;
  private String title;
  private String gradeBand;
  private String description;
  private UUID tenantId;

  public Job() {}

  public Job(
      UUID jobId,
      String jobCode,
      String title,
      String gradeBand,
      String description,
      UUID tenantId) {
    this.jobId = jobId;
    this.jobCode = jobCode;
    this.title = title;
    this.gradeBand = gradeBand;
    this.description = description;
    this.tenantId = tenantId;
  }

  public Job(UUID jobId, String jobCode, String title, String gradeBand) {
    this.jobId = jobId;
    this.jobCode = jobCode;
    this.title = title;
    this.gradeBand = gradeBand;
  }

  public UUID getTenantId() {
    return tenantId;
  }

  public void setTenantId(UUID tenantId) {
    this.tenantId = tenantId;
  }

  public UUID getJobId() {
    return jobId;
  }

  public void setJobId(UUID jobId) {
    this.jobId = jobId;
  }

  public String getJobCode() {
    return jobCode;
  }

  public void setJobCode(String jobCode) {
    this.jobCode = jobCode;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getGradeBand() {
    return gradeBand;
  }

  public void setGradeBand(String gradeBand) {
    this.gradeBand = gradeBand;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public static Job create(String jobCode, String title, String gradeBand) {
    if (jobCode == null || jobCode.isBlank() || title == null || title.isBlank()) {
      throw new IllegalArgumentException("jobCode y title son requeridos");
    }
    return new Job(UUID.randomUUID(), jobCode, title, gradeBand);
  }
}
