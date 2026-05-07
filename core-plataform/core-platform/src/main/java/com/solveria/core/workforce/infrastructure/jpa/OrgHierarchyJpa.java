package com.solveria.core.workforce.infrastructure.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    name = "org_hierarchy",
    indexes = {
      @Index(name = "idx_hierarchy_parent", columnList = "parent_unit_id"),
      @Index(name = "idx_hierarchy_child", columnList = "child_unit_id"),
      @Index(name = "idx_hierarchy_effective", columnList = "effective_date")
    })
@Getter
@Setter
@ToString(exclude = "childUnit")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrgHierarchyJpa {

  @Id
  @Column(name = "hierarchy_id")
  private UUID hierarchyId;

  @Column(name = "parent_unit_id", nullable = false)
  private UUID parentUnitId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "child_unit_id", nullable = false)
  private OrgUnitJpa childUnit;

  @Column(name = "hierarchy_type")
  private String hierarchyType;

  @Column(name = "effective_date", nullable = false)
  private LocalDate effectiveDate;

  @Column(name = "end_date")
  private LocalDate endDate;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

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
    OrgHierarchyJpa that = (OrgHierarchyJpa) o;
    return getHierarchyId() != null && Objects.equals(getHierarchyId(), that.getHierarchyId());
  }

  @Override
  public final int hashCode() {
    return this instanceof HibernateProxy
        ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
        : getClass().hashCode();
  }
}
