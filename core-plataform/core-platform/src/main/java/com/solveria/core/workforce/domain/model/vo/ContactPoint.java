package com.solveria.core.workforce.domain.model.vo;

import jakarta.persistence.Embeddable;
import java.io.Serializable;

/*
 * Value Object: ContactPoint
 *
 * <p>Representa un punto de contacto (email, teléfono, dirección). Es reemplazable; si cambia, se
 * reemplaza completamente (no tiene identidad propia).
 *
 * <p>Invariantes: - Email debe tener formato válido si está presente - Teléfono debe ser formato
 * válido (opcional) - Dirección es de libre formato (opcional)
 */
import java.util.Objects;

@Embeddable
public class ContactPoint implements Serializable {

  private static final long serialVersionUID = 1L;
  private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@(.+)$";

  private String email;
  private String phone;
  private String address;

  public ContactPoint() {}

  public ContactPoint(String email, String phone, String address) {
    this.email = email;
    this.phone = phone;
    this.address = address;
  }

  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }

  public String getPhone() { return phone; }
  public void setPhone(String phone) { this.phone = phone; }

  public String getAddress() { return address; }
  public void setAddress(String address) { this.address = address; }

  public static ContactPoint create(String email, String phone, String address) {
    if (email != null && !email.matches(EMAIL_PATTERN)) {
      throw new IllegalArgumentException("Email format inválido");
    }
    return new ContactPoint(email, phone, address);
  }

  public ContactPoint updateEmail(String newEmail) {
    if (newEmail != null && !newEmail.matches(EMAIL_PATTERN)) {
      throw new IllegalArgumentException("Email format inválido");
    }
    return new ContactPoint(newEmail, this.phone, this.address);
  }

  public ContactPoint updatePhone(String newPhone) {
    return new ContactPoint(this.email, newPhone, this.address);
  }

  public ContactPoint updateAddress(String newAddress) {
    return new ContactPoint(this.email, this.phone, newAddress);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ContactPoint that = (ContactPoint) o;
    return Objects.equals(email, that.email) &&
            Objects.equals(phone, that.phone) &&
            Objects.equals(address, that.address);
  }

  @Override
  public int hashCode() {
    return Objects.hash(email, phone, address);
  }
}
