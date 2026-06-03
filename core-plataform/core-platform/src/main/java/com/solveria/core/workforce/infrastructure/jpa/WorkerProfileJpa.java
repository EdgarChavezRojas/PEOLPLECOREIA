package com.solveria.core.workforce.infrastructure.jpa;

import com.solveria.core.shared.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
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
    name = "worker_profile",
    indexes = {
      @Index(
          name = "idx_worker_profile_relationship_id",
          columnList = "relationship_id",
          unique = true),
      @Index(name = "idx_worker_profile_employee_no", columnList = "employee_no")
    })
@Getter
@Setter
@ToString(exclude = "relationship")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkerProfileJpa extends BaseEntity {

  @Id
  @Column(name = "profile_id")
  private UUID profileId;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "relationship_id", nullable = false, unique = true)
  private RelationshipJpa relationship;

  @Column(name = "employee_no", nullable = false)
  private String employeeNo;

  @Column(name = "department")
  private String department;

  @Column(name = "job_title")
  private String jobTitle;

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
    WorkerProfileJpa that = (WorkerProfileJpa) o;
    return getProfileId() != null && Objects.equals(getProfileId(), that.getProfileId());
  }

  @Override
  public final int hashCode() {
    return this instanceof HibernateProxy
        ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
        : getClass().hashCode();
  }
}
