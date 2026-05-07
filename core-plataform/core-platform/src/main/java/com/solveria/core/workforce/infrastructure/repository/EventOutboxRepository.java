package com.solveria.core.workforce.infrastructure.repository;

import com.solveria.core.workforce.infrastructure.outbox.EventOutbox;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventOutboxRepository extends JpaRepository<EventOutbox, UUID> {

  List<EventOutbox> findByIsPublishedFalseOrderByCreatedAtAsc();
}
