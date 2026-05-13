package com.solveria.core.shared.outbox.port;

import com.solveria.core.shared.events.DomainEvent;

import java.util.List;

public interface EventOutboxPort {
    /**
     * Guarda un evento en el outbox. El evento sera procesado por un scheduler posterior.
     *
     * @param events Evento de dominio
     */
    void publish(List<DomainEvent> events);
}
