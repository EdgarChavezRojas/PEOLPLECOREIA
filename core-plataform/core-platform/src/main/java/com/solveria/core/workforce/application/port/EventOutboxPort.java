package com.solveria.core.workforce.application.port;

import com.solveria.core.shared.events.DomainEvent;

/**
 * Puerto de Salida: Event Outbox Abstracción
 *
 * <p>Responsabilidad: Guardar eventos de dominio para garantizar consistencia eventual. Por ahora
 * es muy simple. Puede expandirse con métodos de polling, etc.
 */
public interface EventOutboxPort {

  /**
   * Guarda un evento en el outbox. El evento será procesado por un scheduler posterior.
   *
   * @param event Evento de dominio
   */
  void publish(DomainEvent event);
}
