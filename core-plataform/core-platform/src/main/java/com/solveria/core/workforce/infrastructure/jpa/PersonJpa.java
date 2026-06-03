package com.solveria.core.workforce.infrastructure.jpa;

import com.solveria.core.shared.base.BaseEntity;
import com.solveria.core.workforce.domain.model.vo.ContactPoint;
import com.solveria.core.workforce.domain.model.vo.Gender;
import com.solveria.core.workforce.domain.model.vo.MaritalStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
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
import org.hibernate.proxy.HibernateProxy;

@Entity
@Table(
    name = "person",
    indexes = {@Index(name = "idx_person_global_id", columnList = "global_id", unique = true)})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonJpa extends BaseEntity {

  @Id
  @Column(name = "person_id")
  private UUID personId;

  @Column(name = "first_name", nullable = false)
  private String firstName;

  @Column(name = "last_name", nullable = false)
  private String lastName;

  @Column(name = "birth_date", nullable = false)
  private LocalDate birthDate;

  @Convert(converter = GenderConverter.class)
  @Column(name = "gender")
  private Gender gender;

  @Enumerated(EnumType.STRING)
  @Column(name = "marital_status", length = 12)
  private MaritalStatus maritalStatus;

  @Column(name = "profession_title")
  private String professionTitle;

  @Column(name = "DNI")
  private String DNI;

  @Column(name = "user_id", unique = true)
  private Long userId;

  @Column(name = "global_id", nullable = false, unique = true)
  private String globalId;

  @Embedded private ContactPoint contactPoint;

  @OneToMany(
      mappedBy = "person",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @Builder.Default
  private List<PartyIdentifierJpa> identifiers = new ArrayList<>();

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
    PersonJpa personJpa = (PersonJpa) o;
    return getPersonId() != null && Objects.equals(getPersonId(), personJpa.getPersonId());
  }

  @Override
  public final int hashCode() {
    return this instanceof HibernateProxy
        ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
        : getClass().hashCode();
  }
}
