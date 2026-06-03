package com.solveria.core.workforce.infrastructure.jpa;

import com.solveria.core.shared.base.BaseEntity;
import com.solveria.core.workforce.domain.model.vo.HeadcountPlan;
import com.solveria.core.workforce.domain.model.vo.PositionStatus;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
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
    name = "position",
    indexes = {
      @Index(name = "idx_position_unit_id", columnList = "unit_id"),
      @Index(name = "idx_position_status", columnList = "pos_status")
    })
@Getter
@Setter
@ToString(exclude = "job")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PositionJpa extends BaseEntity {

  @Id
  @Column(name = "position_id")
  private UUID positionId;

  @Column(name = "unit_id", nullable = false)
  private UUID unitId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "job_id", nullable = false)
  private JobJpa job;

  @Enumerated(EnumType.STRING)
  @Column(name = "pos_status", nullable = false)
  private PositionStatus status;

  @Column(name = "is_budgeted")
  private Boolean isBudgeted;

  @Embedded private HeadcountPlan headcountPlan;

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(name = "position_occupant", joinColumns = @JoinColumn(name = "position_id"))
  @Column(name = "person_id")
  @Builder.Default
  private List<UUID> occupantPersonIds = new ArrayList<>();

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
    PositionJpa that = (PositionJpa) o;
    return getPositionId() != null && Objects.equals(getPositionId(), that.getPositionId());
  }

  @Override
  public final int hashCode() {
    return this instanceof HibernateProxy
        ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
        : getClass().hashCode();
  }
}
