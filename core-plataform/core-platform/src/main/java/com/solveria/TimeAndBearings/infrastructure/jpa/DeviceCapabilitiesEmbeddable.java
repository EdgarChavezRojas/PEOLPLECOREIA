package com.solveria.TimeAndBearings.infrastructure.jpa;

import com.solveria.TimeAndBearings.domain.model.vo.DeviceCapabilities;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Embeddable for {@link DeviceCapabilities}. Mapped inline within {@link ClockingDeviceJpa}. Puro
 * JPA — sin lógica de dominio.
 */
@Embeddable
public class DeviceCapabilitiesEmbeddable {

  @Column(name = "supports_fingerprint", nullable = false)
  private boolean supportsFingerprint;

  @Column(name = "supports_facial", nullable = false)
  private boolean supportsFacial;

  @Column(name = "supports_nfc", nullable = false)
  private boolean supportsNfc;

  @Column(name = "supports_qr", nullable = false)
  private boolean supportsQr;

  @Column(name = "firmware_version", length = 50)
  private String firmwareVersion;

  /** RSA-2048 public key in PEM format. Stored as TEXT to accommodate large keys. */
  @Column(name = "public_key_pem", columnDefinition = "TEXT")
  private String publicKeyPem;

  public DeviceCapabilitiesEmbeddable() {}

  public boolean isSupportsFingerprint() {
    return supportsFingerprint;
  }

  public void setSupportsFingerprint(boolean v) {
    this.supportsFingerprint = v;
  }

  public boolean isSupportsFacial() {
    return supportsFacial;
  }

  public void setSupportsFacial(boolean v) {
    this.supportsFacial = v;
  }

  public boolean isSupportsNfc() {
    return supportsNfc;
  }

  public void setSupportsNfc(boolean v) {
    this.supportsNfc = v;
  }

  public boolean isSupportsQr() {
    return supportsQr;
  }

  public void setSupportsQr(boolean v) {
    this.supportsQr = v;
  }

  public String getFirmwareVersion() {
    return firmwareVersion;
  }

  public void setFirmwareVersion(String v) {
    this.firmwareVersion = v;
  }

  public String getPublicKeyPem() {
    return publicKeyPem;
  }

  public void setPublicKeyPem(String v) {
    this.publicKeyPem = v;
  }
}
