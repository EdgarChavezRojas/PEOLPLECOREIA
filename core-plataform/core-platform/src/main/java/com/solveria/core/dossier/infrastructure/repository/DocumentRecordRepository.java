package com.solveria.core.dossier.infrastructure.repository;

import com.solveria.core.dossier.infrastructure.jpa.DocumentRecordJpa;
import com.solveria.core.dossier.domain.model.vo.DocumentCategory;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRecordRepository extends JpaRepository<DocumentRecordJpa, UUID> {

  Optional<DocumentRecordJpa> findByDocIdAndTenantId(UUID docId, UUID tenantId);
    /**
     * Busca documentos próximos a expirar o ya expirados a nivel global (cross-tenant).
     * @param expiryDate Fecha límite calculada (ej. hoy + 30 días).
     * @return Lista de entidades que requieren evaluación de dominio.
     * * Nota de Idempotencia: Filtra estrictamente por expirationWarningSent = false
     * para garantizar que el motor de reglas no dispare eventos de alerta duplicados (spam).
     */
  List<DocumentRecordJpa>
      findByExpirationWarningSentFalseAndMetadata_ExpiryDateIsNotNullAndMetadata_ExpiryDateLessThanEqual(
          LocalDate expiryDate);

  long countByRelationshipIdAndDocCategoryAndTenantId(
      UUID relationshipId, DocumentCategory docCategory, UUID tenantId);
}
