package com.solveria.core.dossier.application.port;

import com.solveria.core.shared.events.DomainEvent;

/**
 * Puerto de Salida: Event Outbox Abstraccion
 *
 * <p>Responsabilidad: Guardar eventos de dominio para garantizar consistencia eventual.
 */
public interface EventOutboxPort {

  /**
   * Guarda un evento en el outbox. El evento sera procesado por un scheduler posterior.
   *
   * @param event Evento de dominio
   */
  void publish(DomainEvent event);
}
