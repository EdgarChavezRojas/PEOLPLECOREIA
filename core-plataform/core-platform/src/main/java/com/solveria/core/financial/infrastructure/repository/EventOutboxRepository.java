package com.solveria.core.financial.infrastructure.repository;

import com.solveria.core.financial.infrastructure.outbox.EventOutbox;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA Repository: Event Outbox para BC Financial. */
@Repository
public interface EventOutboxRepository extends JpaRepository<EventOutbox, UUID> {}
