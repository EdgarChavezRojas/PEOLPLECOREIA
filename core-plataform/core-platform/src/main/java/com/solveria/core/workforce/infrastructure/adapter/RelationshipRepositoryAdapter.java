package com.solveria.core.workforce.infrastructure.adapter;

import com.solveria.core.security.context.SecurityTenantContext;
import com.solveria.core.shared.outbox.application.port.EventOutboxPort;
import com.solveria.core.workforce.application.port.RelationshipRepositoryPort;
import com.solveria.core.workforce.domain.model.AcademicProfile;
import com.solveria.core.workforce.domain.model.Relationship;
import com.solveria.core.workforce.domain.model.StatusLog;
import com.solveria.core.workforce.domain.model.WorkerProfile;
import com.solveria.core.workforce.domain.model.vo.RelationshipStatus;
import com.solveria.core.workforce.domain.model.vo.RelationshipType;
import com.solveria.core.workforce.infrastructure.jpa.AcademicProfileJpa;
import com.solveria.core.workforce.infrastructure.jpa.RelationshipJpa;
import com.solveria.core.workforce.infrastructure.jpa.StatusLogJpa;
import com.solveria.core.workforce.infrastructure.jpa.WorkerProfileJpa;
import com.solveria.core.workforce.infrastructure.mapper.AcademicProfileMapper;
import com.solveria.core.workforce.infrastructure.mapper.RelationshipMapper;
import com.solveria.core.workforce.infrastructure.mapper.StatusLogMapper;
import com.solveria.core.workforce.infrastructure.mapper.WorkerProfileMapper;
import com.solveria.core.workforce.infrastructure.repository.RelationshipRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RelationshipRepositoryAdapter implements RelationshipRepositoryPort {

  private final RelationshipRepository relationshipRepository;
  private final RelationshipMapper relationshipMapper;
  private final EventOutboxPort eventOutboxPort;
  private final AcademicProfileMapper academicProfileMapper;
  private final WorkerProfileMapper workerProfileMapper;
  private final StatusLogMapper statusLogMapper;

  @Override
  @Transactional
  public Relationship save(Relationship relationship) {
    RelationshipJpa relationshipJpa =
        relationshipRepository
            .findById(relationship.getRelationshipId())
            .map(
                existing -> {
                  relationshipMapper.updateJpa(relationship, existing);
                  mergeAcademicProfile(relationship, existing);
                  mergeWorkerProfile(relationship, existing);
                  mergeStatusLogs(relationship, existing);
                  return existing;
                })
            .orElseGet(() -> relationshipMapper.toJpa(relationship));
    RelationshipJpa savedRelationshipJpa = relationshipRepository.save(relationshipJpa);
    Relationship savedRelationship = relationshipMapper.toDomain(savedRelationshipJpa);

    eventOutboxPort.publish(relationship.pullDomainEvents());

    return savedRelationship;
  }

  @Override
  public Optional<Relationship> findByRelationshipIdAndTenantId(
      UUID relationshipId, UUID tenantId) {
    String currentTenantIdStr = SecurityTenantContext.getCurrentTenantId();
    UUID currentTenantId;
    if (currentTenantIdStr == null || currentTenantIdStr.isBlank()) {
      currentTenantId = tenantId;
    } else {
      currentTenantId = UUID.fromString(currentTenantIdStr);
      if (!currentTenantId.equals(tenantId)) {
        return Optional.empty();
      }
    }
    return relationshipRepository
        .findByRelationshipIdAndTenantId(relationshipId, currentTenantId)
        .map(relationshipMapper::toDomain);
  }

  @Override
  public boolean existsPrimaryRelationshipForPersonInTenant(UUID personId, UUID tenantId) {
    String currentTenantIdStr = SecurityTenantContext.getCurrentTenantId();
    UUID currentTenantId;
    if (currentTenantIdStr == null || currentTenantIdStr.isBlank()) {
      currentTenantId = tenantId;
    } else {
      currentTenantId = UUID.fromString(currentTenantIdStr);
      if (!currentTenantId.equals(tenantId)) {
        return false;
      }
    }
    return relationshipRepository.existsByPersonIdAndTenantIdAndRelationTypeAndCurrentStatus(
            personId, currentTenantId, RelationshipType.LABOR, RelationshipStatus.ACTIVE)
        || relationshipRepository.existsByPersonIdAndTenantIdAndRelationTypeAndCurrentStatus(
            personId, currentTenantId, RelationshipType.EMPLOYEE, RelationshipStatus.ACTIVE);
  }

  @Override
  public List<Relationship> findByPersonId(UUID personId) {
    List<RelationshipJpa> jpaEntities = relationshipRepository.findByPersonId(personId);

    return jpaEntities.stream()
        .map(relationshipMapper::toDomain) // Transforma cada JPA a Modelo de Dominio
        .toList();
  }

  @Override
  public List<Relationship> findAll() {
    List<RelationshipJpa> jpaEntities = relationshipRepository.findAll();

    return jpaEntities.stream()
        .map(relationshipMapper::toDomain) // Transforma cada JPA a Modelo de Dominio
        .toList();
  }

  private void mergeAcademicProfile(Relationship relationship, RelationshipJpa existing) {
    AcademicProfile domainProfile = relationship.getAcademicProfile();
    if (domainProfile == null) {
      return;
    }
    AcademicProfileJpa target = existing.getAcademicProfile();
    if (target == null) {
      AcademicProfileJpa created = academicProfileMapper.toJpa(domainProfile);
      created.setRelationship(existing);
      created.setTenantId(existing.getTenantId());
      existing.setAcademicProfile(created);
      return;
    }
    academicProfileMapper.updateJpa(domainProfile, target);
    target.setRelationship(existing);
    target.setTenantId(existing.getTenantId());
  }

  private void mergeWorkerProfile(Relationship relationship, RelationshipJpa existing) {
    WorkerProfile domainProfile = relationship.getWorkerProfile();
    if (domainProfile == null) {
      return;
    }
    WorkerProfileJpa target = existing.getWorkerProfile();
    if (target == null) {
      WorkerProfileJpa created = workerProfileMapper.toJpa(domainProfile);
      created.setRelationship(existing);
      created.setTenantId(existing.getTenantId());
      existing.setWorkerProfile(created);
      return;
    }
    workerProfileMapper.updateJpa(domainProfile, target);
    target.setRelationship(existing);
    target.setTenantId(existing.getTenantId());
  }

  private void mergeStatusLogs(Relationship relationship, RelationshipJpa existing) {
    if (relationship.getStatusLogs() == null || relationship.getStatusLogs().isEmpty()) {
      return;
    }
    if (existing.getStatusLogs() == null) {
      existing.setStatusLogs(new ArrayList<>());
    }
    Map<UUID, StatusLogJpa> existingById =
        existing.getStatusLogs().stream()
            .collect(Collectors.toMap(StatusLogJpa::getLogId, Function.identity()));
    for (StatusLog log : relationship.getStatusLogs()) {
      if (log == null || log.getLogId() == null || existingById.containsKey(log.getLogId())) {
        continue;
      }
      StatusLogJpa created = statusLogMapper.toJpa(log);
      created.setRelationship(existing);
      created.setTenantId(existing.getTenantId());
      existing.getStatusLogs().add(created);
    }
  }
}
