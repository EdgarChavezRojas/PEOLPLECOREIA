# 01_Workforce_Org_Master.md

## 1. BC: Workforce & Organization Master (El "Núcleo Operativo")
* **Dominios agrupados:** 1. Org Structure, 2. Person & Identity, 3. Relationship & Lifecycle, 4. Position & Headcount.
* **Por qué esta agrupación:** Estos cuatro dominios comparten la entidad "Persona" y su ubicación. Separarlos obligaría a constantes consultas inter-módulos.
* **Explicación técnica:** Resuelve la **Unicidad de la Persona** y el modelo **Persona ≠ Relación ≠ Asignación** en un solo esquema transaccional, garantizando un historial consistente en la estructura.

---

## Agregados del Contexto

**Agregado 1: PersonIdentity**
* **Root:** `Person`. Ancla de la identidad civil; sobrevive a cualquier contrato.
* **Contenido:**
  * `PartyIdentifier` (Entity): Documentos (CI, Pasaporte) con ciclo de vida, fechas de emisión/caducidad.
  * `ContactPoint` (VO): Si un email/teléfono cambia, se reemplaza el valor completo.

**Agregado 2: EmploymentRelationship**
* **Root:** `Relationship`. Puente legal que controla si el vínculo es laboral, académico o de pasantía.
* **Contenido:**
  * `WorkerProfile` / `AcademicProfile` (Entity): Evolucionan y mantienen identidad vinculada a la relación específica.
  * `StatusLog` (Entity): Rastrea la máquina de estados (Onboarding, Activo, Suspendido).

**Agregado 3: OrgStructure**
* **Root:** `OrgUnit`. Nodo central de la jerarquía (Sucursal, Facultad).
* **Contenido:**
  * `OrgHierarchy` (Entity): Define el "padre" y el tipo de relación; se versionan en el tiempo.
  * `CostCenter` (VO): Etiqueta contable inmutable.

**Agregado 4: PositionPlaza**
* **Root:** `Position`. La "silla" física/virtual; existe independientemente del ocupante.
* **Contenido:**
  * `Job` (Reference/VO): Descriptor del cargo con atributos estandarizados.
  * `HeadcountPlan` (VO): Límite presupuestario de la plaza.

---

## Políticas Asociadas
**Clúster de Políticas: Carga Horaria y Academia (Eje Universitario)**
* **P3: Política de Multi-Rol Académico-Administrativo:** Solo Universidades. Permite poseer simultáneamente un WorkerProfile y un AcademicProfile bajo contratos distintos, validando colisión de horas.
* **P4: Política de Contratación por Carga Horaria:** Los contratos docentes se rigen por Vigencia Semestral/Materia automatizando suspensiones/reactivaciones.

---

## Workflows Orquestados

### 1. Workflow de Onboarding Integral (Alta de Colaborador)
* **Alcance:** Dominios 1, 2, 3, 4, 5, 6.
* **Agregados involucrados:** PersonIdentity, EmploymentRelationship, EmploymentAgreement, PositionPlaza.
* **Descripción Técnica:** Crea identidad, define vínculo, asigna plaza y formaliza contrato. Varía por Tenant (Retail es rápido; ONG exige donante; Educación exige validación de títulos).
* **Invariantes Críticas:** No duplicidad de CI, HeadcountPlan con cupo, Sueldo >= Bs 3.300, FundingSource con saldo (ONG), Títulos validados (Educación).
* **Flujo (Happy Path):** 1. Solicitud. 
  2. Validación Identidad (Deduplicación). 
  3. Definición de Relación/Perfil. 
  4. Selección/Bloqueo de Plaza. 
  5. Generación de Propuesta (Effective Dating). 
  6. Carga Documental. 
  7. Validación y Firma (SoD). 
  8. Activación en fecha efectiva.

### 4. Workflow de Transferencia y Movilidad Territorial
* **Alcance:** Dominios 1, 10.
* **Agregados involucrados:** OrgStructure, BudgetFunding.
* **Descripción Técnica:** Gestiona cambio de empleado entre sucursales o centros de costo (Santa Cruz).
* **Flujo:** Solicitud -> Validación de Presupuesto Local -> Configuración de Split de Costos (100% de prorrateo exacto) -> Preservación de Beneficios Regionales (INFOCAL y Feriados se mantienen) -> Logística de Activos (AssetCustody).

---

## Eventos de Dominio

* **`PERSON_CREATED` (Sync):** Crea la raíz de identidad civil. Estado Draft.
* **`PERSON_MASTER_CREATED` (Async):** Eleva estado a Master tras validación. Notifica al Kardex.
* **`PERSON_DEDUPLICATION_MATCH_FOUND` (Sync/Bloqueante):** Detiene flujo al detectar CI duplicado. Crítico para reingresos y mantener antigüedad.
* **`PERSON_UPDATED` (Async):** Notifica a Payroll/Kardex tras cambios en datos maestros.
* **`RELATIONSHIP_CREATED` (Sync):** Disparado por Onboarding aprobado. Crea vínculo legal.
* **`RELATIONSHIP_REACTIVATED` (Sync):** Disparado por reincorporaciones. Restablece relación y línea de antigüedad.
* **`RELATIONSHIP_ENDED` (Sync):** Inicia Offboarding. Activa cronómetro 15 días (Finiquito) y notifica Assets.
* **`ORG_UNIT_ASSIGNED_CHANGED` (Async):** Actualiza imputación analítica tras transferencias internas.
* **`ORG_UNIT_GEOGRAPHIC_MOVED` (Async):** Actualiza coordenadas y alerta sobre cambios en tasas regionales (INFOCAL).
* **`POSITION_ASSIGNED` (Sync):** Vincula al trabajador con una plaza presupuestada.
* **`POSITION_VACATED` (Async):** Notifica a Reclutamiento que la plaza está disponible.
* **`ACADEMIC_PROFILE_RANK_UPDATED` (Sync):** (Educación) Actualiza categoría del docente y dispara adenda salarial automática.

---

## Diccionario de Datos

**Aggregate 1: PersonIdentity**
| Entidad / VO | Campo | Tipo de Dato | Restricciones / Lógica |
| :--- | :--- | :--- | :--- |
| **Person** (Root) | `person_id` | UUID | PK. Identificador global único. |
| | `first_name` | VARCHAR(100) | Obligatorio. |
| | `last_name` | VARCHAR(100) | Obligatorio. |
| | `birth_date` | DATE | **Invariante**: Debe ser >= 18 años. |
| | `gender` | ENUM | Varón, Mujer. |
| | `global_id` | VARCHAR(50) | Único. Registro maestro inalterable. |
| **PartyIdentifier** | `identifier_id` | UUID | PK. |
| | `person_id` | UUID | FK a Person. |
| | `id_type` | ENUM | CI, Pasaporte. |
| | `id_number` | VARCHAR(30) | Alfanumérico único. |
| | `extension` | ENUM | Departamental (SC, LP, etc.). |
| | `issue_date` | DATE | Fecha de emisión. |
| | `expiry_date` | DATE | Control de vigencia documental. |
| **ContactPoint** | `email` | VARCHAR(150) | Reemplazable (VO). |
| | `phone` | VARCHAR(20) | Reemplazable. |
| | `address` | TEXT | Domicilio civil. |

**Aggregate 2: EmploymentRelationship**
| Entidad / VO | Campo | Tipo de Dato | Restricciones / Lógica |
| :--- | :--- | :--- | :--- |
| **Relationship** (Root) | `relationship_id` | UUID | PK. Soporta "Multi-vínculo". |
| | `person_id` | UUID | FK a Person. |
| | `tenant_id` | UUID | ID de la empresa. |
| | `rel_type` | ENUM | Laboral, Académico, Pasantía. |
| | `current_status` | ENUM | Draft, Active, Suspended, Terminated. |
| | `hire_date` | DATE | Fecha oficial de ingreso. |
| **WorkerProfile** | `profile_id` | UUID | PK. |
| | `employee_no` | VARCHAR(20) | Número de legajo interno. |
| **AcademicProfile**| `academic_id` | UUID | PK. Exclusivo Educación. |
| | `current_rank` | ENUM | Auxiliar, Adjunto, Titular. |
| | `teaching_load` | INT | Límite de carga horaria. |
| **StatusLog** | `log_id` | UUID | PK. |
| | `previous_status` | ENUM | Estado anterior. |
| | `new_status` | ENUM | Estado nuevo. |
| | `change_reason` | TEXT | Justificación del cambio. |

**Aggregate 3: OrgStructure**
| Entidad / VO | Campo | Tipo de Dato | Restricciones / Lógica |
| :--- | :--- | :--- | :--- |
| **OrgUnit** (Root)| `unit_id` | UUID | PK. |
| | `parent_id` | UUID | FK. **Estructura de Árbol Único**. |
| | `name` | VARCHAR(150) | Nombre de la unidad. |
| | `unit_type` | ENUM | Administrativa, Académica, Comercial. |
| | `geo_coords` | POINT/TEXT | Para marcación por geocerca. |
| **OrgHierarchy** | `hierarchy_id` | UUID | PK. |
| | `effective_date` | DATE | Fecha vigencia de estructura. |
| **CostCenter** | `cost_code` | VARCHAR(50) | Etiqueta contable inmutable. |

**Aggregate 4: PositionPlaza**
| Entidad / VO | Campo | Tipo de Dato | Restricciones / Lógica |
| :--- | :--- | :--- | :--- |
| **Position** (Root)| `position_id` | UUID | PK. |
| | `unit_id` | UUID | FK a OrgUnit. |
| | `pos_status` | ENUM | Vacant, Occupied, Reserved. |
| | `is_budgeted` | BOOLEAN | Control de respaldo financiero. |
| **Job** | `job_code` | VARCHAR(20) | Descriptor de cargo. |
| | `title` | VARCHAR(100) | Título del cargo. |
| | `grade_band` | VARCHAR(10) | Banda salarial asociada. |
| **HeadcountPlan** | `max_slots` | INT | **Invariante**: Límite estricto. |
| | `current_slots` | INT | Ocupación actual. |