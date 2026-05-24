package com.solveria.core.workforce.infrastructure.jpa;

import com.solveria.core.shared.base.BaseEntity;
import com.solveria.core.workforce.domain.model.OrgUnit;
import com.solveria.core.workforce.domain.model.vo.CostCenter;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
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
    name = "org_unit",
    indexes = {
      @Index(name = "idx_org_unit_tenant_id", columnList = "tenant_id"),
      @Index(name = "idx_org_unit_parent_id", columnList = "parent_id")
    })
@Getter
@Setter
@ToString(exclude = "orgHierarchies")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrgUnitJpa extends BaseEntity {

  @Id
  @Column(name = "unit_id")
  private UUID unitId;


  @Column(name = "parent_id")
  private UUID parentId;

  @Column(name = "name", nullable = false)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(name = "unit_type")
  private OrgUnit.OrgUnitType unitType;

  @Embedded private CostCenter costCenter;

  @Column(name = "geo_coords")
  private String geoCoords;

  @Column(name = "is_root", nullable = false)
  private Boolean isRoot;

  @OneToMany(
      mappedBy = "childUnit",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @Builder.Default
  private List<OrgHierarchyJpa> orgHierarchies = new ArrayList<>();


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
    OrgUnitJpa that = (OrgUnitJpa) o;
    return getUnitId() != null && Objects.equals(getUnitId(), that.getUnitId());
  }

  @Override
  public final int hashCode() {
    return this instanceof HibernateProxy
        ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
        : getClass().hashCode();
  }
}
