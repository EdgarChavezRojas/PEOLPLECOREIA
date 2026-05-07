package com.solveria.core.dossier.infrastructure.repository;

import com.solveria.core.dossier.infrastructure.jpa.DocumentRecordJpa;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRecordRepository extends JpaRepository<DocumentRecordJpa, UUID> {

  Optional<DocumentRecordJpa> findByDocIdAndTenantId(UUID docId, UUID tenantId);
}
