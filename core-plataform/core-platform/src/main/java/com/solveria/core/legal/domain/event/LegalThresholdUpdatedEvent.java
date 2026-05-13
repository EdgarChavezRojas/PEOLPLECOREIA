package com.solveria.core.legal.domain.event;


import com.solveria.core.shared.events.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LegalThresholdUpdatedEvent(UUID policyRuleId, String ruleName, BigDecimal newValue, Instant occurredAt)
        implements DomainEvent {}
