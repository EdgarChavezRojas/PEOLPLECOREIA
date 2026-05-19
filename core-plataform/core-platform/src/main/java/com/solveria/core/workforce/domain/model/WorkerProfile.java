package com.solveria.core.workforce.domain.model;

import java.util.UUID;

/**
 * Entity: WorkerProfile
 *
 * <p>Atributos operativos para roles laborales. Evoluciona (cambios de nivel, atributos operativos)
 * y mantiene identidad vinculada a la relación laboral específica.
 */

public class WorkerProfile {

  private UUID profileId;
  private UUID relationshipId;
  private String employeeNo;
  private String department;
  private String jobTitle;

  public WorkerProfile() {}

  public WorkerProfile(UUID profileId, UUID relationshipId, String employeeNo, String department, String jobTitle) {
    this.profileId = profileId;
    this.relationshipId = relationshipId;
    this.employeeNo = employeeNo;
    this.department = department;
    this.jobTitle = jobTitle;
  }

  public UUID getProfileId() { return profileId; }
  public void setProfileId(UUID profileId) { this.profileId = profileId; }

  public UUID getRelationshipId() { return relationshipId; }
  public void setRelationshipId(UUID relationshipId) { this.relationshipId = relationshipId; }

  public String getEmployeeNo() { return employeeNo; }
  public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }

  public String getDepartment() { return department; }
  public void setDepartment(String department) { this.department = department; }

  public String getJobTitle() { return jobTitle; }
  public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

  public static WorkerProfile create(UUID relationshipId, String employeeNo, String department, String jobTitle) {
    if (relationshipId == null || employeeNo == null || employeeNo.isBlank()) {
      throw new IllegalArgumentException("relationshipId y employeeNo son requeridos");
    }
    return new WorkerProfile(UUID.randomUUID(), relationshipId, employeeNo, department, jobTitle);
  }
}
