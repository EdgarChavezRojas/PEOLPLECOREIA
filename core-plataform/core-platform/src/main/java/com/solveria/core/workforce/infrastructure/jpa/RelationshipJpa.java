package com.solveria.core.workforce.infrastructure.jpa;

import com.solveria.core.workforce.domain.model.vo.RelationshipStatus;
import com.solveria.core.workforce.domain.model.vo.RelationshipType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
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
    name = "relationship",
    indexes = {
      @Index(name = "idx_relationship_person_id", columnList = "person_id"),
      @Index(name = "idx_relationship_tenant_id", columnList = "tenant_id"),
      @Index(name = "idx_relationship_status", columnList = "current_status")
    })
@Getter
@Setter
@ToString(exclude = {"workerProfile", "academicProfile", "statusLogs"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RelationshipJpa {

  @Id
  @Column(name = "relationship_id")
  private UUID relationshipId;

  @Column(name = "person_id", nullable = false)
  private UUID personId;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Enumerated(EnumType.STRING)
  @Column(name = "rel_type", nullable = false)
  private RelationshipType relationType;

  @Enumerated(EnumType.STRING)
  @Column(name = "current_status", nullable = false)
  private RelationshipStatus currentStatus;

  @Column(name = "hire_date")
  private LocalDate hireDate;

  @OneToOne(mappedBy = "relationship", cascade = CascadeType.ALL, orphanRemoval = true)
  private WorkerProfileJpa workerProfile;

  @OneToOne(mappedBy = "relationship", cascade = CascadeType.ALL, orphanRemoval = true)
  private AcademicProfileJpa academicProfile;

  @OneToMany(
      mappedBy = "relationship",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @Builder.Default
  private List<StatusLogJpa> statusLogs = new ArrayList<>();

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDate createdAt;

  @Column(name = "updated_at")
  private LocalDate updatedAt;

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
    RelationshipJpa that = (RelationshipJpa) o;
    return getRelationshipId() != null
        && Objects.equals(getRelationshipId(), that.getRelationshipId());
  }

  @Override
  public final int hashCode() {
    return this instanceof HibernateProxy
        ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
        : getClass().hashCode();
  }
}
