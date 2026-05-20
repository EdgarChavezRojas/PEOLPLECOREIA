package com.solveria.TimeAndBearings.domain.model.enums;

/**
 * Rol del dispositivo dentro de su OrgUnit. Diccionario de Datos BC-TM v1.2 –
 * ClockingDevice.device_role.
 *
 * <p>Invariante de Unicidad (Device Uniqueness): por cada combinación (org_unit_id, device_type)
 * solo puede existir UN ClockingDevice {@code PRIMARY} en estado {@code ACTIVE}. Los roles {@code
 * SECONDARY} y {@code BACKUP} no tienen restricción de unicidad.
 */
public enum DeviceRole {

  /**
   * Dispositivo principal de la OrgUnit para ese tipo. Sujeto a la Invariante de Unicidad de
   * Dispositivo Primario.
   */
  PRIMARY,

  /** Dispositivo secundario (refuerzo operacional). Sin restricción de unicidad. */
  SECONDARY,

  /** Dispositivo de respaldo. Sin restricción de unicidad. */
  BACKUP
}
