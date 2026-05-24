package com.solveria.core.workforce.infrastructure.jpa;

import com.solveria.core.shared.base.BaseEntity;
import com.solveria.core.workforce.domain.model.vo.RelationshipStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.proxy.HibernateProxy;

@Entity
@Table(
    name = "status_log",
    indexes = {
      @Index(name = "idx_status_log_relationship_id", columnList = "relationship_id"),
      @Index(name = "idx_status_log_changed_at", columnList = "changed_at")
    })
@Getter
@Setter
@ToString(exclude = "relationship")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusLogJpa extends BaseEntity {

  @Id
  @Column(name = "log_id")
  private UUID logId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "relationship_id", nullable = false)
  private RelationshipJpa relationship;

  @Enumerated(EnumType.STRING)
  @Column(name = "previous_status")
  private RelationshipStatus previousStatus;

  @Enumerated(EnumType.STRING)
  @Column(name = "new_status", nullable = false)
  private RelationshipStatus newStatus;

  @Column(name = "change_reason")
  private String changeReason;

  @Column(name = "changed_at", nullable = false)
  private LocalDate changedAt;

  @Column(name = "changed_by")
  private UUID changedBy;

  @Override
  public final boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    Class<?> oEffectiveClass =
        o instanceof HibernateProxy
            ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass()
            : o.getClass();
    Class<?> thisEffectiveClass =
        this instanceof HibernateProxy
            ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass()
            : this.getClass();
    if (thisEffectiveClass != oEffectiveClass) return false;
    StatusLogJpa that = (StatusLogJpa) o;
    return getLogId() != null && Objects.equals(getLogId(), that.getLogId());
  }

  @Override
  public final int hashCode() {
    return this instanceof HibernateProxy
        ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
        : getClass().hashCode();
  }
}
