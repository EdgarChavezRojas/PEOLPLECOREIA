package com.solveria.core.workforce.infrastructure.jpa;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

@Entity
@Table(
    name = "job",
    indexes = {@Index(name = "idx_job_code", columnList = "job_code", unique = true)})
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobJpa {

  @Id
  @Column(name = "job_id")
  private UUID jobId;

  @Column(name = "job_code", nullable = false, unique = true)
  private String jobCode;

  @Column(name = "title", nullable = false)
  private String title;

  @Column(name = "grade_band")
  private String gradeBand;

  @Column(name = "description")
  private String description;

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
    JobJpa jobJpa = (JobJpa) o;
    return getJobId() != null && Objects.equals(getJobId(), jobJpa.getJobId());
  }

  @Override
  public final int hashCode() {
    return this instanceof HibernateProxy
        ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
        : getClass().hashCode();
  }
}
