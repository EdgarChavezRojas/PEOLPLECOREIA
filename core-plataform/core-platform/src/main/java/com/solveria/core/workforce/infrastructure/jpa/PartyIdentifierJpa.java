package com.solveria.core.workforce.infrastructure.jpa;

import com.solveria.core.shared.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
    name = "party_identifier",
    indexes = {
      @Index(name = "idx_identifier_person_id", columnList = "person_id"),
      @Index(name = "idx_identifier_id_number", columnList = "id_number", unique = true),
      @Index(name = "idx_identifier_expiry", columnList = "expiry_date")
    })
@Getter
@Setter
@ToString(exclude = "person")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartyIdentifierJpa extends BaseEntity {

  @Id
  @Column(name = "identifier_id")
  private UUID identifierId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "person_id", nullable = false)
  private PersonJpa person;

  @Column(name = "id_type", nullable = false)
  private String idType;

  @Column(name = "id_number", nullable = false, unique = true)
  private String idNumber;

  @Column(name = "extension")
  private String extension;

  @Column(name = "issue_date")
  private LocalDate issueDate;

  @Column(name = "expiry_date")
  private LocalDate expiryDate;


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
    PartyIdentifierJpa that = (PartyIdentifierJpa) o;
    return getIdentifierId() != null && Objects.equals(getIdentifierId(), that.getIdentifierId());
  }

  @Override
  public final int hashCode() {
    return this instanceof HibernateProxy
        ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
        : getClass().hashCode();
  }
}
