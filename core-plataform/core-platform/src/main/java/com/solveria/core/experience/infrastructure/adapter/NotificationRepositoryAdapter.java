package com.solveria.core.experience.infrastructure.adapter;

import com.solveria.core.experience.application.port.out.NotificationPO;
import com.solveria.core.experience.domain.model.Notification;
import com.solveria.core.experience.infrastructure.jpa.NotificationJpa;
import com.solveria.core.experience.infrastructure.mapper.NotificationMapper;
import com.solveria.core.experience.infrastructure.repository.NotificationRepository;
import com.solveria.core.security.context.SecurityTenantContext;
import com.solveria.core.shared.outbox.application.port.EventOutboxPort;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Adapter: NotificationPO. Persiste Notification y publica eventos al outbox. */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationRepositoryAdapter implements NotificationPO {

  private final NotificationRepository repository;
  private final NotificationMapper mapper;
  private final EventOutboxPort eventOutboxPort;

  @Override
  @Transactional
  public void save(Notification notification) {

    NotificationJpa jpa =
        repository
            .findById(notification.getNotificationId())
            .map(
                existing -> {
                  mapper.updateJpa(existing, notification);
                  return existing;
                })
            .orElseGet(() -> mapper.toJpa(notification));

    repository.save(jpa);
    eventOutboxPort.publish(notification.pullDomainEvents());
  }

  @Override
  public Optional<Notification> findById(UUID notificationId) {
    return repository.findById(notificationId).map(mapper::toDomain);
  }

  @Override
  public List<Notification> findByRecipientId(UUID recipientId) {
    UUID tenantId = UUID.fromString(SecurityTenantContext.getCurrentTenantId());
    return repository.findByRecipientIdAndTenantId(recipientId, tenantId).stream()
        .map(mapper::toDomain)
        .toList();
  }
}
