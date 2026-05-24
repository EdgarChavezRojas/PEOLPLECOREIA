package com.solveria.core.experience.infrastructure.repository;

import com.solveria.core.experience.infrastructure.jpa.NotificationJpa;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationJpa, UUID> {

  List<NotificationJpa> findByRecipientIdAndTenantId(UUID recipientId, UUID tenantId);
}
