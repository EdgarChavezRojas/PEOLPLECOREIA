package com.solveria.core.workforce.infrastructure.mapper;

import com.solveria.core.workforce.domain.model.WorkerProfile;
import com.solveria.core.workforce.infrastructure.jpa.WorkerProfileJpa;
import org.springframework.stereotype.Component;

@Component
public class WorkerProfileMapper {

  public WorkerProfileJpa toJpa(WorkerProfile workerProfile) {
    if (workerProfile == null) {
      return null;
    }
    return WorkerProfileJpa.builder()
        .profileId(workerProfile.getProfileId())
        .employeeNo(workerProfile.getEmployeeNo())
        .department(workerProfile.getDepartment())
        .jobTitle(workerProfile.getJobTitle())
        .build();
  }

  public WorkerProfile toDomain(WorkerProfileJpa jpa) {
    if (jpa == null) {
      return null;
    }
    WorkerProfile workerProfile = new WorkerProfile();
    workerProfile.setProfileId(jpa.getProfileId());
    workerProfile.setEmployeeNo(jpa.getEmployeeNo());
    workerProfile.setDepartment(jpa.getDepartment());
    workerProfile.setJobTitle(jpa.getJobTitle());
    if (jpa.getRelationship() != null) {
      workerProfile.setRelationshipId(jpa.getRelationship().getRelationshipId());
    }
    return workerProfile;
  }

  public void updateJpa(WorkerProfile workerProfile, WorkerProfileJpa target) {
    if (workerProfile == null || target == null) {
      return;
    }
    target.setEmployeeNo(workerProfile.getEmployeeNo());
    target.setDepartment(workerProfile.getDepartment());
    target.setJobTitle(workerProfile.getJobTitle());
  }
}
