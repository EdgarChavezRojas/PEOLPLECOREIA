package com.solveria.core.shared.outbox.infrastructure.repository;

import com.solveria.core.shared.outbox.domain.OutboxState;
import com.solveria.core.shared.outbox.infrastructure.jpa.SharedOutboxMessageJpaEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SharedOutboxRepository extends JpaRepository<SharedOutboxMessageJpaEntity, UUID> {

    List<SharedOutboxMessageJpaEntity> findTop50ByStateOrderByCreatedAtAsc(OutboxState state);
}

