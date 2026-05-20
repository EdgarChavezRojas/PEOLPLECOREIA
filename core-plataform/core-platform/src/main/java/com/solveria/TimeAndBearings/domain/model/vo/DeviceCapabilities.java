package com.solveria.TimeAndBearings.domain.model.vo;

/**
 * Value Object: capacidades físicas/lógicas de un ClockingDevice. Diccionario de Datos BC-TM v1.2 –
 * Agregado 15, DeviceCapabilities.
 *
 * <p>Puro Java 21 Record — ninguna anotación Spring/JPA. La clave pública RSA-2048 ({@code
 * publicKeyPem}) se usa para verificar la firma digital de cada TimeEntry proveniente de KIOSK o
 * BIOMETRIC_READER (Invariante Device Signature Integrity).
 *
 * <p>La clave privada NUNCA se almacena en el servidor; se instala directamente en el hardware
 * durante el proceso WF-TM04 paso 2.
 *
 * @param supportsFingerprint Soporta huella dactilar (P-TM29 Nivel 1).
 * @param supportsFacial Soporta reconocimiento facial con liveness (P-TM29 Nivel 2/3).
 * @param supportsNfc Soporta marcación por tarjeta NFC.
 * @param supportsQr Soporta marcación por QR de sesión.
 * @param firmwareVersion Versión del firmware instalado en el dispositivo.
 * @param publicKeyPem Clave pública RSA-2048 en formato PEM. NOT NULL para KIOSK/BIOMETRIC_READER.
 */
public record DeviceCapabilities(
    boolean supportsFingerprint,
    boolean supportsFacial,
    boolean supportsNfc,
    boolean supportsQr,
    String firmwareVersion,
    String publicKeyPem) {

  /**
   * Factory: canal móvil — solo facial y QR, sin clave pública fija (usa la del SO del
   * dispositivo).
   */
  public static DeviceCapabilities mobileChannel(String firmwareVersion) {
    return new DeviceCapabilities(false, true, false, true, firmwareVersion, null);
  }

  /** Factory: lector biométrico estándar (huella + facial) con clave pública RSA-2048. */
  public static DeviceCapabilities biometricReader(String firmwareVersion, String publicKeyPem) {
    if (publicKeyPem == null || publicKeyPem.isBlank()) {
      throw new IllegalArgumentException(
          "publicKeyPem is mandatory for BIOMETRIC_READER (Invariante Device Signature Integrity).");
    }
    return new DeviceCapabilities(true, true, false, false, firmwareVersion, publicKeyPem);
  }
}
