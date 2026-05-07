package com.solveria.core.experience.infrastructure.repository;

import com.solveria.core.experience.infrastructure.outbox.ExperienceEventOutbox;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExperienceEventOutboxRepository
    extends JpaRepository<ExperienceEventOutbox, UUID> {}
