# Guía Completa de Implementación: Casos de Uso Pendientes - Módulo Workforce (BC 1)

Este documento detalla el paso a paso técnico para implementar **todos** los Casos de Uso restantes del Bounded Context **Workforce & Org Master**, siguiendo los principios de Arquitectura Hexagonal y Domain-Driven Design (DDD) establecidos en tus especificaciones y eventos de dominio.

---

## 1. Dominio: Person & Identity (Identidad y Datos Maestros)

### 1.1. UpdatePersonUseCase
* **Objetivo:** Actualizar el estado civil (`marital_status`), la profesión (`profession_title`) o medios de contacto (`ContactPoint`). Obligatorio para Finiquitos.
* **Implementación:**
  1. Cargar la entidad usando `PersonRepositoryPort.findByGlobalId(...)` o ID interno.
  2. Modificar los campos `maritalStatus`, `professionTitle` y actualizar la lista de `ContactPoint`.
  3. Guardar con `PersonRepositoryPort.save(...)`.
  4. Emitir `PersonUpdatedEvent`.

### 1.2. ResolveDeduplicationUseCase
* **Objetivo:** Unificar registros cuando el sistema detecta un posible registro duplicado.
* **Implementación:**
  1. Cargar los dos registros de `Person` involucrados.
  2. Aplicar lógica de fusión (merge) para dejar un registro como principal y marcar el otro como inactivo/fusionado.
  3. Guardar ambos estados actualizados.
  4. Emitir `PersonDeduplicationMatchFoundEvent`.

---

## 2. Dominio: Employment Relationship (Ciclo de Vida Laboral)

### 2.1. AssignPersonToPositionUseCase
* **Objetivo:** Vincular al empleado con una "silla" específica (cambia estado de `VACANT` a `OCCUPIED`).
* **Implementación:**
  1. Obtener la vacante mediante `PositionRepositoryPort.findByPositionIdAndTenantId`.
  2. Validar que su estado sea `VACANT` y validar límite de Headcount si aplica.
  3. Llamar al método del dominio `position.occupy(personId)`.
  4. Guardar la posición.
  5. Emitir `PositionAssignedEvent`.

### 2.2. UpdateAcademicRankUseCase
* **Objetivo:** Subir el rango (ej. Adjunto a Titular) en el `AcademicProfile` (Educación).
* **Implementación:**
  1. Buscar la relación laboral con `RelationshipRepositoryPort.findByRelationshipIdAndTenantId`.
  2. Validar que la relación sea de tipo `ACADEMIC`.
  3. Actualizar el rango dentro del `AcademicProfile`.
  4. Registrar la acción en el `StatusLog`.
  5. Guardar la relación.
  6. Emitir `AcademicProfileRankUpdatedEvent`.

### 2.3. UpdateEmploymentConditionsUseCase
* **Objetivo:** Cambiar condiciones contractuales, como pasar de Plazo Fijo (PF) a Permanente (PE).
* **Implementación:**
  1. Cargar la `Relationship`.
  2. Modificar el tipo/condición de la relación laboral.
  3. Añadir registro al `StatusLog` por modificación de contrato.
  4. Guardar la relación actualizada en el repositorio.

### 2.4. TerminateRelationshipUseCase
* **Objetivo:** Dar de baja al empleado (gatillo inicial para el Workflow de Finiquito).
* **Implementación:**
  1. Recuperar la `Relationship` actual.
  2. Cambiar el `RelationshipStatus` a `TERMINATED`.
  3. Añadir el motivo en el `StatusLog` (ej. Renuncia, Despido).
  4. Guardar los cambios en `RelationshipRepositoryPort`.
  5. Emitir `RelationshipEndedEvent`.

### 2.5. ReactivateRelationshipUseCase
* **Objetivo:** Reingresar a un ex-empleado sin crear una persona nueva.
* **Implementación:**
  1. Buscar la relación inactiva o crear un nuevo periodo activo vinculado a la misma persona.
  2. Configurar el `StatusLog` como reactivado o nuevo contrato.
  3. Guardar en la base de datos.
  4. Emitir `RelationshipReactivatedEvent`.

---

## 3. Dominio: Position & Headcount (Plazas y Presupuestos)

### 3.1. VacatePositionUseCase
* **Objetivo:** Liberar la plaza para que pueda ser reasignada tras una baja o traslado.
* **Implementación:**
  1. Buscar la plaza usando `PositionRepositoryPort.findByPositionIdAndTenantId`.
  2. Cambiar el estado a `VACANT` y remover el `personId` asignado.
  3. Guardar la posición actualizada.
  4. Emitir `PositionVacatedEvent`.

### 3.2. UpdatePositionBudgetUseCase
* **Objetivo:** Cambiar la configuración financiera de la plaza (ej. de `isBudgeted` falso a verdadero).
* **Implementación:**
  1. Cargar la plaza existente.
  2. Actualizar la bandera financiera de presupuesto.
  3. Guardar los cambios.

---

## 4. Dominio: Organization Structure (Jerarquía)

### 4.1. MoveOrgUnitUseCase / RestructureHierarchyUseCase
* **Objetivo:** Cambiar la dependencia (nodo padre) de un departamento.
* **Implementación:**
  1. Cargar la unidad a mover (`OrgUnit`) y la nueva unidad destino.
  2. Validar que no exista recursividad circular (el padre no puede ser hijo del movido).
  3. Actualizar el puntero `parentId`.
  4. Guardar la unidad en `OrgUnitRepositoryPort`.
  5. Emitir `OrgUnitAssignedChangedEvent`.

### 4.2. RelocateOrgUnitUseCase
* **Objetivo:** Modificar la ubicación geográfica (ej. Campus/Sede) de una unidad organizativa.
* **Implementación:**
  1. Buscar la `OrgUnit`.
  2. Modificar el bloque de extensión geográfica (`Extension`).
  3. Guardar la actualización.
  4. Emitir `OrgUnitGeographicMovedEvent`.
