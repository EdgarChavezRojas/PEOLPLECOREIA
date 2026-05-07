package com.solveria.core.workforce.infrastructure.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
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
    name = "academic_profile",
    indexes = {
      @Index(
          name = "idx_academic_profile_relationship_id",
          columnList = "relationship_id",
          unique = true),
      @Index(name = "idx_academic_profile_rank", columnList = "current_rank")
    })
@Getter
@Setter
@ToString(exclude = "relationship")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicProfileJpa {

  @Id
  @Column(name = "academic_id")
  private UUID academicId;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "relationship_id", nullable = false, unique = true)
  private RelationshipJpa relationship;

  @Column(name = "current_rank")
  private String currentRank;

  @Column(name = "teaching_load")
  private Integer teachingLoad;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

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
    AcademicProfileJpa that = (AcademicProfileJpa) o;
    return getAcademicId() != null && Objects.equals(getAcademicId(), that.getAcademicId());
  }

  @Override
  public final int hashCode() {
    return this instanceof HibernateProxy
        ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
        : getClass().hashCode();
  }
}
