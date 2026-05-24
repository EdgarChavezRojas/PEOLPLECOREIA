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

**3. Contexto: Dossier & Talent (Expediente y Activos)**

**Gestión de evidencias documentales y responsabilidad material.**

Digital Kardex & Compliance Documental

**DOCUMENT\_RECORDED**

- **Gatillo y Naturaleza (Async): Carga manual de un archivo vía ESS/MSS o integración con escáner. Es Asincrónico para permitir el procesamiento de *storage*, generación de miniaturas y cálculo de integridad.**
- **Lógica Funcional y Efectos: Genera una entrada en DocumentRecord. Se calcula un Hash (SHA-256) para garantizar la inalterabilidad ante auditorías del Ministerio de Trabajo.**
- **UI e IA:**
  - **UI: Barra de progreso y confirmación de "Documento en revisión".**
  - **IA: OCR para pre-llenar campos (ej: número de carnet, fecha de emisión) y detección de imágenes borrosas.**
- **Localización: El metadato debe incluir la "Regional" del documento (ej. CI emitido en Santa Cruz).**
- **Invariantes: Un documento no validado no puede activar una regla de elegibilidad.**

**DOCUMENT\_VALIDATION\_REJECTED**

- **Gatillo y Naturaleza (Sync): Acción manual del Analista de RRHH tras revisar la evidencia. Es Sincrónico para notificar al usuario de inmediato.**
- **Lógica Funcional: El estado cambia a Rejected. Dispara una notificación al ESS. Si el documento era "Crítico", puede disparar un ELIGIBILITY\_SUSPENDED.**
- **UI e IA:**
  - **UI: Modal de comentarios obligatorios para explicar el rechazo.**
  - **IA: Sugiere al usuario cómo corregir el documento basado en el motivo del rechazo.**
- **Invariantes: Mantiene el historial de versiones; el documento rechazado no se borra, se marca como inválido para auditoría.**

**DOCENT\_ACADEMIC\_TITLE\_VERIFIED**

- **Gatillo y Naturaleza (Sync): Validación técnica (títulos en Provisión Nacional). Es Sincrónico para asegurar invariantes de jerarquía.**
- **Lógica Funcional: Actualiza el AcademicProfile. Notifica al motor de Escalafón para habilitar posibles ascensos de categoría docente.**
- **UI e IA:**
  - **IA: Compara el título con el descriptor del cargo para detectar brechas de formación (*Skill Gap*).**
- **Localización: Específico para el Tenant Educación. Valida contra la base de datos de títulos reconocidos en Bolivia.**

**HEALTH\_CARD\_EXPIRATION\_WARNING**

- **Gatillo y Naturaleza (Async): Disparado por un *Worker* de monitoreo cronológico (T-30 días).**
- **Lógica Funcional: Notifica al colaborador y al supervisor. Si es Retail, el sistema marca la plaza como "En riesgo de inhabilitación".**
- **UI e IA:**
  - **IA: Predice el impacto operativo si 5 cajeros pierden su carnet sanitario simultáneamente en la misma sucursal.**
- **Localización: Crítico para Retail en Santa Cruz (Sedeges/Alcaldía).**

**MANDATORY\_COMPLIANCE\_DOC\_MISSING**

- **Gatillo y Naturaleza (Sync): Motor de reglas al intentar cambiar el estado de la relación a "Activo". Bloqueante.**
- **Lógica Funcional: Impide el cierre del Onboarding. El sistema no genera el alta en planilla hasta que la matriz de cumplimiento esté al 100%.**
- **Impacto en Invariantes: Bloqueo por Ilicitud: No se puede activar un cajero sin antecedentes policiales o un médico sin título verificado.**

**ELIGIBILITY\_SUSPENDED\_BY\_COMPLIANCE**

- **Gatillo y Naturaleza (Sync): Automático al expirar un documento crítico.**
- **Lógica Funcional: Interceptor que bloquea al empleado en el módulo de Scheduling (Turnos). No puede ser asignado a una grilla horaria.**
- **UI e IA:**
  - **UI: El nombre del empleado aparece en gris/rojo con el icono "Suspendido por Compliance".**
- **Invariantes: El colaborador sigue teniendo una Relationship activa (para efectos de antigüedad), pero su elegibilidad operativa es nula.**

**ELIGIBILITY\_RESTORED**

- **Gatillo y Naturaleza (Sync): Disparado tras la validación de un documento de reemplazo.**
- **Lógica Funcional: Libera los bloqueos en el módulo de Turnos y Nómina.**
- **UI e IA:**
  - **IA: Analiza el tiempo que el empleado estuvo "fuera de servicio" para KPIs de cumplimiento.**

Assets & Custodia de Bienes

**ASSET\_LOANED\_TO\_WORKER**

- **Gatillo y Naturaleza (Sync): Registro de entrega física. Requiere firma digital del empleado (OTP o Firma Simple).**
- **Lógica Funcional: Crea el vínculo AssignedAsset. El valor del activo se registra en el expediente del trabajador.**
- **UI e IA:**
  - **UI: Generación automática del Acta de Entrega en PDF.**
- **Invariantes: Un activo no puede ser prestado si ya figura como asignado a otra persona.**

**ASSET\_TRANSFER\_REQUIRED**

- **Gatillo y Naturaleza (Async): Reacción al evento ORG\_UNIT\_GEOGRAPHIC\_MOVED.**
- **Lógica Funcional: Alerta al área de Activos Fijos que el equipo asignado debe moverse físicamente entre sedes (ej. de Sede Norte a Sede Equipetrol).**
- **Localización: Específico para traslados territoriales en Santa Cruz.**

**ASSET\_RETURNED**

- **Gatillo y Naturaleza (Sync): Recepción de equipo por parte de Almacén/TI.**
- **Lógica Funcional: Libera la responsabilidad del trabajador. El activo vuelve al estado "Disponible".**
- **UI e IA:**
  - **IA: Evalúa el ciclo de vida del activo (desgaste) comparado con el tiempo de uso.**

**ASSET\_DAMAGE\_REPORTED**

- **Gatillo y Naturaleza (Sync): Registro de incidente. Puede ser disparado por el empleado (ESS) o supervisor.**
- **Lógica Funcional: Documenta el daño con evidencia fotográfica. Dispara un flujo de "Conciliación de Daño" para decidir si aplica descuento por planilla (previa revisión legal).**
- **Impacto en Invariantes: Si el daño es crítico, el activo se marca como "No Operativo" pero sigue bajo custodia hasta su baja técnica.**

**OFFBOARDING\_BLOCKED\_BY\_ASSETS**

- **Gatillo y Naturaleza (Sync): Guardrail Bloqueante en el cierre de la relación laboral.**
- **Lógica Funcional: El proceso de liquidación financiera (Finiquito) no puede avanzar a "Aprobado" si existen activos marcados como "En Custodia".**
- **UI e IA:**
  - **UI: Alerta roja en el panel de Offboarding: "Pendiente: 1 Laptop, 1 Celular".**
- **Impacto en Invariantes: Certificación de Devolución: Protege el patrimonio de la empresa antes de emitir el cheque de beneficios sociales.**


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