# 03_Employee_Dossier_Talent_Foundations

## 3. BC: Employee Dossier & Talent Foundations (La "Memoria Institucional")
* **Dominios agrupados:** 6. Digital Kardex & Document Compliance, 7. Assets & Equipment Assignment, 15. Talent Inventory.
* **Por qué esta agrupación:** Forman el "Expediente Integral". El "Talento" incluye títulos académicos y herramientas tecnológicas (Assets).
* **Explicación técnica:** Centraliza la Gobernanza Documental. Valida que un docente posea título y carnet sanitario vigente para dar clases.

---

## Agregados del Contexto

**Agregado 7: DigitalKardex**
* **Root:** `DocumentRecord`. Entrada única de un documento legal.
* **Contenido:**
  * `ValidationStatus` (VO): Estado del documento (Aprobado/Rechazado).
  * `DocumentMetadata` (VO): Datos del archivo (hash, storage ID) inmutables tras carga.

**Agregado 8: AssetCustody**
* **Root:** `AssignedAsset`. Registro de responsabilidad que vincula un activo a una persona.
* **Contenido:**
  * `AssetDescriptor` (VO): Características técnicas del equipo.

---

## Políticas Asociadas
**Clúster de Políticas: Ciclo de Vida y Documentación**
* **P7: Política de Vigencia Documental Crítica:** Exige carnet sanitario (Retail) y títulos (Educación). El DigitalKardex bloquea el estado "Activo" o emite alertas al caducar.
* **P8: Política de Alerta Temprana de Quinquenio:** (Aunque pertenece al eje de antigüedad, se apoya en el expediente documental). Al detectar 60 meses, consolida derecho indemnizatorio (Multa 30% tras 30 días de solicitud).

---

## Workflows Orquestados

### 5. Workflow de Verificación de Compliance Documental
* **Alcance:** Dominios 6, 9.
* **Agregados involucrados:** DigitalKardex.
* **Descripción Técnica:** Orquesta carga, validación y auditoría de documentos obligatorios (P7). 
* **Flujo:** Gatillo (Alta/Renovación T-30) -> Carga de Documento (Hash) -> Validación -> Monitoreo de Vigencia -> Suspensión Automática si expira (Bloquea turnos) -> Restauración.

### 8. Workflow de Asignación y Recuperación de Activos
* **Alcance:** Dominio 7.
* **Agregados involucrados:** AssetCustody.
* **Descripción Técnica:** Registra entrega de equipos y bloquea cierre administrativo si existen activos pendientes de devolución.
* **Flujo:** Necesidad -> Registro de Entrega (Acta Digital) -> Auditoría -> Gatillo de Offboarding -> Bloqueo "Blocked by Assets" -> Devolución Exitosa o Daño/Descuento -> Liberación.

### 10. Workflow de Mérito y Escalafón Docente (Solo Educación)
* **Alcance:** Dominio 15.
* **Agregados involucrados:** PersonIdentity (Academic Profile).
* **Descripción Técnica:** Gestiona la subida de categoría docente basada en méritos y años (P3, P4).
* **Flujo:** Detección de Elegibilidad -> Carga de Evidencias -> Validación de Títulos (P7) -> Evaluación Comisión -> Aprobación (RANK_UPDATED) -> Actualización Salarial.

---

## Eventos de Dominio

* **`DOCUMENT_RECORDED` (Async):** Genera entrada y calcula Hash (SHA-256) para inalterabilidad.
* **`DOCUMENT_VALIDATION_REJECTED` (Sync):** Cambia estado a Rejected tras auditoría RRHH.
* **`DOCENT_ACADEMIC_TITLE_VERIFIED` (Sync):** Actualiza AcademicProfile y notifica motor de Escalafón.
* **`HEALTH_CARD_EXPIRATION_WARNING` (Async):** Disparado a T-30 días.
* **`MANDATORY_COMPLIANCE_DOC_MISSING` (Sync/Bloqueante):** Impide cierre de Onboarding sin matriz 100%.
* **`ELIGIBILITY_SUSPENDED_BY_COMPLIANCE` (Sync):** Bloquea al empleado en Scheduling por documento expirado.
* **`ELIGIBILITY_RESTORED` (Sync):** Libera bloqueos tras validar documento.
* **`ASSET_LOANED_TO_WORKER` (Sync):** Crea AssignedAsset (requiere firma).
* **`ASSET_TRANSFER_REQUIRED` (Async):** Alerta movimiento de activo tras transferencia de sede.
* **`ASSET_RETURNED` (Sync):** Libera responsabilidad.
* **`ASSET_DAMAGE_REPORTED` (Sync):** Registro de incidente (conduce a conciliación).
* **`OFFBOARDING_BLOCKED_BY_ASSETS` (Sync/Bloqueante):** Impide Finiquito si hay activos en custodia.

---

## Diccionario de Datos

**Aggregate 7: DigitalKardex**
| Entidad / VO | Campo | Tipo de Dato | Restricciones / Lógica |
| :--- | :--- | :--- | :--- |
| **DocumentRecord** (Root)| `doc_id` | UUID | PK. |
| | `relationship_id` | UUID | FK. |
| | `doc_category` | ENUM | Identidad, Salud, Académico. |
| | `doc_type` | VARCHAR(50) | Ej: "Carnet Sanitario". |
| | `is_critical` | BOOLEAN | **Invariante**: TRUE bloquea elegibilidad. |
| **ValidationStatus** | `current_state` | ENUM | Pending, Approved, Rejected, Expired. |
| | `reviewer_id` | UUID | Validador. |
| | `review_date` | TIMESTAMP | Fecha última validación. |
| | `reject_reason` | TEXT | Justificación. |
| **DocumentMetadata** | `storage_id` | UUID | Puntero al archivo. |
| | `file_name` | VARCHAR(255) | Nombre archivo. |
| | `hash_sha256` | VARCHAR(64) | **Invariante RAG**: Inalterabilidad. |
| | `expiry_date` | DATE | Fecha de caducidad. |

**Aggregate 8: AssetCustody**
| Entidad / VO | Campo | Tipo de Dato | Restricciones / Lógica |
| :--- | :--- | :--- | :--- |
| **AssignedAsset** (Root) | `assignment_id` | UUID | PK. |
| | `worker_id` | UUID | FK a WorkerProfile. |
| | `asset_tag` | VARCHAR(50) | Código único. |
| | `status` | ENUM | Custodia, Devuelto, Dañado. |
| | `assigned_at` | TIMESTAMP | Firma acta entrega. |
| | `returned_at` | TIMESTAMP | Recepción física. |
| **AssetDescriptor** | `category` | ENUM | Computación, Herramientas, etc. |
| | `tech_specs` | JSONB | Características. |
| | `initial_state` | TEXT | Estado entrega. |

**Entidades: Talent & Performance**
| Entidad | Campo | Tipo de Dato | Restricciones / Lógica |
| :--- | :--- | :--- | :--- |
| **PerformanceSnapshot** | `snapshot_id` | UUID | PK. |
| | `eval_period` | VARCHAR(20) | Gestión (Q1-2026). |
| | `score` | DECIMAL(5,2)| Calificación numérica. |
| **SkillSet** | `skill_id` | UUID | PK. |
| | `skill_name` | VARCHAR(100) | Competencia. |
| | `proficiency` | ENUM | Básico, Avanzado, etc. |
| **TrainingHistory** | `training_id` | UUID | PK. |
| | `course_name` | VARCHAR(200) | Título formación. |
| | `doc_id` | UUID | FK a Kardex. |