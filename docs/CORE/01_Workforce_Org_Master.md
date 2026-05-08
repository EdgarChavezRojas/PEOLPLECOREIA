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

 **PERSON\_CREATED**
- **Gatillo y Naturaleza: Sincrónico. Se dispara manualmente desde el módulo de reclutamiento o vía API desde un ATS externo al registrar los datos básicos de un ciudadano.**
- **Lógica Funcional: Crea la raíz de identidad civil. Es el "ancla" que sobrevive a cualquier contrato. No genera un vínculo laboral aún, solo una entidad Person en estado Draft.**
- **UI e IA:**
- **UI: Formulario de alta rápida con validación de máscara para CI/Pasaporte.**
- **IA: El motor de *Data Quality* califica la completitud del perfil para predecir la viabilidad del proceso de contratación.**
- **Localización: El sistema debe permitir tipos de identidad bolivianos (CI con complemento) y extranjeros (Pasaporte).**
- **Invariantes: Garantiza la creación de un GlobalID único antes de proceder a cualquier validación de duplicidad.**
- **PERSON\_MASTER\_CREATED**
- **Gatillo y Naturaleza: Asincrónico. Se dispara tras la validación exitosa de la identidad contra fuentes oficiales o procesos internos de auditoría.**
- **Lógica Funcional: Eleva el estado de la persona a Master. Este evento notifica al Digital Kardex para que genere la estructura de carpetas necesaria para los documentos obligatorios.**
- **UI e IA:**
- **UI: El perfil del usuario muestra un check de "Identidad Verificada".**
- **IA: Identifica patrones demográficos (edad, ubicación) para sugerir beneficios o seguros de salud adecuados.**
- **Localización: Valida que el CI tenga el formato correcto según el departamento emisor en Bolivia.**
- **Invariantes: Protege la regla de Identidad Única: una persona física solo posee un registro maestro en el sistema.**
- **PERSON\_DEDUPLICATION\_MATCH\_FOUND**
- **Gatillo y Naturaleza: Sincrónico (Bloqueante). Se dispara automáticamente cuando el motor de reglas detecta un CI o pasaporte ya existente en la base de datos histórica.**
- **Lógica Funcional: Detiene el flujo de creación. Es crítico para el reingreso de personal, ya que permite recuperar la antigüedad acumulada para el Bono de Antigüedad e Indemnización.**
- **UI e IA:**
- **UI: Modal de bloqueo que muestra el perfil existente y solicita permiso para realizar un *Merge* o reactivación.**
- **IA: Realiza *Fuzzy Matching* en nombres y fechas de nacimiento para detectar duplicados incluso con errores de digitación en el CI.**
- **Invariantes: Evita registros duplicados que causarían pagos dobles de beneficios sociales o inconsistencias en la Gestora Pública.**
- **PERSON\_UPDATED**
- **Gatillo y Naturaleza: Asincrónico. Se dispara tras la edición de datos maestros (apellidos por matrimonio, cambio de domicilio o estado civil).**
- **Lógica Funcional: Notifica a los módulos de Payroll (para RC-IVA) y Digital Kardex (para solicitar nuevos documentos como el Certificado de Matrimonio).**
- **UI e IA:**
- **UI: Feed de auditoría visual indicando el "antes" y el "después".**
- **IA: Si el cambio de domicilio es a otro departamento (ej. de Santa Cruz a La Paz), la IA sugiere revisar las políticas de INFOCAL regional.**
- **Invariantes: Mantiene la integridad del historial (Effective Dating). El cambio no borra el dato anterior, crea una nueva versión con fecha de vigencia.**
- **RELATIONSHIP\_CREATED**
- **Gatillo y Naturaleza: Sincrónico. Disparado por la aprobación final del flujo de *Onboarding*.**
- **Lógica Funcional: Crea el vínculo legal entre la Person y el Tenant. Define si el perfil será WorkerProfile o AcademicProfile.**
- **UI e IA:**
- **UI: El estado del colaborador cambia a Onboarding o Active.**
- **IA: Predice el tiempo estimado de rampa (Onboarding time) basado en el tipo de tenant (Retail es más rápido que Educación).**
- **Invariantes: Valida la regla de No Traslape de Vínculos Primarios para evitar contingencias legales por doble percepción.**
- **RELATIONSHIP\_REACTIVATED**
- **Gatillo y Naturaleza: Sincrónico. Disparado en casos de reincorporación tras bajas largas o reingresos tras finiquitos previos.**
- **Lógica Funcional: Restablece la relación y dispara el recálculo de la línea de tiempo de antigüedad en el AccrualVault.**
- **Localización: En Bolivia, el sistema debe verificar si el reingreso ocurre en menos de 90 días para determinar si la antigüedad se mantiene ininterrumpida.**
- **Invariantes: Protege la continuidad laboral para el cálculo de vacaciones (P13) y quinquenios (P8).**
- **RELATIONSHIP\_ENDED**
- **Gatillo y Naturaleza: Sincrónico (Inicia el Offboarding). Disparado por renuncia, despido o fallecimiento.**
- **Lógica Funcional: Cierra el expediente y activa el cronómetro de 15 días calendario para el pago del Finiquito (P17). Notifica al módulo de Assets para la devolución de equipos.**
- **UI e IA:**
- **IA: Análisis de *Churn* para identificar si la salida se debe a factores de clima laboral o competitividad salarial.**
- **Invariantes: Libera la plaza en el Headcount Management y bloquea accesos al ESS.**

**ORG\_UNIT\_ASSIGNED\_CHANGED**

- **Gatillo y Naturaleza: Asincrónico. Disparado por una transferencia interna (movimiento entre sucursales o departamentos).**
- **Lógica Funcional: Actualiza la imputación analítica en Budget Control. El costo laboral se moverá de un centro de costos a otro.**
- **Localización: Si el cambio es entre tiendas de Retail en Santa Cruz, se prorratea el costo entre tiendas para el P&L mensual.**
- **Invariantes: Garantiza la regla de No Unidades Huérfanas: el colaborador siempre debe pertenecer a un nodo jerárquico.**

**ORG\_UNIT\_GEOGRAPHIC\_MOVED**

- **Gatillo y Naturaleza: Asincrónico. Disparado cuando una unidad administrativa física se traslada (ej. la sede central se muda de zona).**
- **Lógica Funcional: Actualiza las coordenadas para el módulo de marcación por geocerca (Time & Attendance).**
- **Localización: Afecta el cálculo de INFOCAL si la unidad sale del departamento de Santa Cruz (tasa del 1% deja de aplicar).**

**POSITION\_ASSIGNED**

- **Gatillo y Naturaleza: Sincrónico. Vincula oficialmente al trabajador con una plaza presupuestada.**
- **Lógica Funcional: Valida que la posición tenga presupuesto y que el sueldo pactado sea >= Bs 3.300 (SMN 2026).**
- **UI e IA:**
  - **UI: Actualización del organigrama en tiempo real.**
- **Invariantes: Control de Plazas (Headcount): impide asignar personal si no hay plazas vacantes autorizadas.**

**POSITION\_VACATED**

- **Gatillo y Naturaleza: Asincrónico. Disparado cuando una persona deja su puesto (promoción o retiro).**
- **Lógica Funcional: Notifica a Reclutamiento (ATS) que la plaza está disponible para ser publicada.**
- **UI e IA:**
  - **IA: Recomienda candidatos internos para sucesión basados en el Talent Inventory.**

**ACADEMIC\_PROFILE\_RANK\_UPDATED**

- **Gatillo y Naturaleza: Sincrónico (Tenant Educación). Disparado por la aprobación de un ascenso en el escalafón docente por méritos académicos.**
- **Lógica Funcional: Actualiza la categoría del docente (ej. de Auxiliar a Titular). Dispara automáticamente una adenda salarial por cambio de rango.**
- **UI e IA:**
  - **UI: Notificación de felicitación al docente en su ESS.**
- **Invariantes: Valida que el docente tenga los títulos académicos necesarios registrados y validados en el Kardex Digital antes de permitir el ascenso.**
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