package com.solveria.TimeAndBearings.infrastructure.listener;

import com.solveria.TimeAndBearings.domain.event.BiometricEnrollmentRevokedEvent;
import com.solveria.core.shared.events.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class TimeAndBearingsIntegrationConsumer {

    private static final Logger log = LoggerFactory.getLogger(TimeAndBearingsIntegrationConsumer.class);

    @Async
    @EventListener
    public void on(DomainEvent event) {
        if (event instanceof BiometricEnrollmentRevokedEvent revokedEvent) {
            log.info("Integracion: enrolamiento biometrico revocado ledgerId={} tenantId={}",
                    revokedEvent.getClass().getSimpleName(), revokedEvent.tenantId());
        } else {
            log.debug("Integracion: evento recibido tipo={}", event.getClass().getSimpleName());
        }
    }
}

