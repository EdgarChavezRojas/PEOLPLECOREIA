package com.solveria.core.experience.infrastructure.jpa;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** JPA Entity: Notification. Tabla: experience_notification. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "experience_notification")
public class NotificationJpa {

  @Id
  @Column(name = "notif_id", nullable = false, updatable = false)
  private UUID notificationId;

  @Column(name = "recipient_id", nullable = false)
  private UUID recipientId;

  @Column(name = "channel", length = 20, nullable = false)
  @Enumerated(EnumType.STRING)
  private com.solveria.core.experience.domain.model.vo.NotificationChannel channel;

  @Column(name = "subject", length = 255, nullable = false)
  private String subject;

  @Column(name = "body", columnDefinition = "TEXT")
  private String body;

  @Column(name = "tenant_id", length = 50, nullable = false)
  private String tenantId;

  @Column(name = "sent_at", nullable = false, updatable = false)
  private Instant sentAt;

  /** Auditoría de notificación legal: cuándo fue leída. */
  @Column(name = "read_at")
  private Instant readAt;
}
