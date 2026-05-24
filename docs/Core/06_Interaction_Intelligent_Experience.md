# 06_Interaction_Intelligent_Experience.md

## 6. BC: Interaction & Intelligent Experience (La "Capa Externa")
* **Dominios agrupados:** 8. Employee & Manager Self-Service (ESS/MSS), 16. AI Insights & Predictive Analytics.
* **Por qué esta agrupación:** Es la "cara" del sistema. ESS/MSS es el canal y la IA es el procesador cualitativo que entrega alertas proactivas.
* **Explicación técnica:** Permite predecir impactos (ej. provisión de fondos para 5 quinquenios inminentes).

---

## Agregados del Contexto

**Agregado 11: PredictiveInsight**
* **Root:** `PredictionModel`. Orquesta análisis para generar alertas.
* **Contenido:**
  * `RiskAlert` (VO): Mensaje inmutable de advertencia de pasivos.

---

## Workflows Orquestados

### 3. Workflow de Renovación Preventiva (Evitar Tácita Reconducción)
* **Alcance:** Dominios 5, 16.
* **Agregados involucrados:** EmploymentAgreement, PredictiveInsight.
* **Descripción Técnica:** Orquestado por Predictive Insights para obligar una decisión (P12).
* **Flujo:** Disparo Cron (T-90) -> Evaluación de Continuidad -> Validación de Límite (Max 2 renovaciones) -> Conversión a Indefinido o Terminación.

### 11. Workflow de Actualización de Datos Personales (ESS/MSS)
* **Propósito:** Empleado solicita cambios (dirección, banco) empoderando al trabajador.
* **Flujo:** Solicitud ESS -> Clasificación (Dato Menor vs Crítico) -> Carga de Evidencia -> Auditoría de Veracidad (MSS) -> Actualización con Historial -> Sincronización Nómina.

### 14. Workflow de Certificaciones y Constancias Digitales
* **Propósito:** Emisión automática y descongestión de RRHH (ESS con firma QR).
* **Flujo:** Solicitud -> Validación de Elegibilidad -> Extracción Dinámica -> Generación PDF (Certificado digital/QR Zero-Trust) -> Validación Pública.

---

## Eventos de Dominio

**6. Contexto: Intelligent Interaction (Experiencia y Procesos Orquestados)**

**Eventos que cierran procesos de usuario o disparan interacciones inteligentes.**

**ONBOARDING\_COMPLETED**

- **Gatillo y Naturaleza (Async): Se dispara automáticamente cuando el motor de estados confirma que los 4 pilares están validados: Identidad (Persona), Vínculo (Relationship), Reglas (Contract) y Ubicación (Position).**
- **Lógica Funcional y Efectos: Activa la elegibilidad en el motor de Payroll y Scheduling. Notifica al módulo de Assets para la entrega de equipos.**
- **UI e IA:**
  - **UI: Confeti visual en el ESS y habilitación del carnet digital.**
  - **IA: Genera un "Perfil de Riesgo de Deserción Temprana" comparando el tiempo de onboarding con la media del tenant.**
- **Invariantes: Impide que un empleado reciba sueldo sin tener un contrato firmado y una posición asignada.**

**OFFBOARDING\_INITIATED**

- **Gatillo y Naturaleza (Sync): Registro manual de la baja. Es Sincrónico para bloquear inmediatamente la creación de nuevos turnos en el futuro.**
- **Lógica Funcional: Dispara el cálculo de la P15 (Promedio 90 días) y P17 (Finiquito). Notifica a TI para la suspensión de accesos en la effective\_to.**
- **UI e IA:**
  - **IA: Análisis de sentimiento en la causa de baja para detectar problemas de liderazgo en unidades específicas.**
- **Impacto en Invariantes: Bloquea cualquier intento de pago de "Bono de Antigüedad" posterior a la fecha de cese.**

**DATA\_CHANGE\_REQUESTED / REJECTED**

- **Gatillo y Naturaleza (Async): Solicitud desde el ESS por el empleado.**
- **Lógica Funcional: Los cambios no impactan PersonMaster hasta que RRHH valide el respaldo (ej. Certificado de matrimonio para cambio de apellido).**
- **UI e IA:**
  - **UI: Línea de tiempo que muestra el estado "Pendiente de validación".**
- **Invariantes: Garantiza la Inalterabilidad de Auditoría: no se puede cambiar un dato civil sin una evidencia documental vinculada.**

**DISCIPLINARY\_ACTION\_REPORTED / MEMORANDUM\_ISSUED**

- **Gatillo y Naturaleza (Sync): Reporte de falta por un supervisor (MSS).**
- **Lógica Funcional: Genera una entrada en el Digital Kardex. Si es MEMORANDUM\_ISSUED, se crea el PDF inalterable con Hash SHA-256.**
- **Localización: En Bolivia, este evento es la base legal para el despido justificado (Art. 16 LGT). El sistema debe clasificar si la falta es "Grave" según el Reglamento Interno del Tenant.**
- **Impacto en Invariantes: Ningún memorándum puede eliminarse una vez emitido; solo puede anularse con una nota de rectificación.**

**MEMORANDUM\_ACKNOWLEDGED**

- **Gatillo y Naturaleza (Sync): Firma digital o acuse de recibo del empleado en el ESS.**
- **Lógica Funcional: Cierra el ciclo de notificación legal. Si el empleado rechaza la firma, el sistema habilita el flujo de "Notificación por Testigos".**
- **UI e IA:**
  - **IA: Identifica si el empleado tiene un patrón de "Faltas próximas a feriados de Santa Cruz" (ej. 24 de septiembre).**

**DISCIPLINARY\_THRESHOLD\_REACHED**

- **Gatillo y Naturaleza (Async): Motor de reglas de cumplimiento.**
- **Lógica Funcional: Alerta automática cuando un empleado acumula (ej.) 3 memorandos por la misma causa en 6 meses.**
- **UI e IA:**
  - **UI: Alerta roja al Gerente de RRHH: "Riesgo Legal: Posibilidad de Despido Justificado".**
- **Impacto en Invariantes: Protege la regla de "Proporcionalidad": Alerta si se intenta despedir sin el historial documental necesario.**

**SUBSTITUTION\_INITIATED / COMPLETED**

- **Gatillo y Naturaleza (Sync): Asignación temporal de una plaza por ausencia del titular.**
- **Lógica Funcional: Crea una TemporaryAssignment. Calcula el "Recargo por Suplencia" (Diferencia salarial).**
- **Diseño para Localización: En Universidades, este evento gestiona el "Docente Reemplazante" por materia, vinculando el AcademicProfile temporalmente al grupo.**
- **Invariantes: Evita que el suplente adquiera derechos de "Plazo Indefinido" en la plaza si la suplencia excede los límites de la política del tenant.**

**CERTIFICATE\_REQUESTED / GENERATED / VALIDATED\_EXTERNALLY**

- **Gatillo y Naturaleza (Async): Solicitud autónoma del empleado.**
- **Lógica Funcional: El sistema extrae datos de Person, Contract y Payroll para generar el PDF. El QR apunta a una URL de validación pública (Zero-Trust).**
- **UI e IA:**
  - **UI: Descarga inmediata en PDF.**
  - **IA: Detecta si hay una alta demanda de certificados para "Entidades Bancarias", sugiriendo una tendencia de sobre-endeudamiento en la plantilla.**
- **Invariantes: Solo se generan certificados si el empleado no tiene bloqueos por documentos vencidos en el Digital Kardex.**


---

## Diccionario de Datos

**Aggregate 11: PredictiveInsight**
| Entidad / VO | Campo | Tipo de Dato | Restricciones / Lógica |
| :--- | :--- | :--- | :--- |
| **PredictionModel** (Root)| `model_id` | UUID | PK. |
| | `model_type` | ENUM | CHURN, LIABILITY_RISK. |
| | `version` | VARCHAR(20) | Versión del modelo (IA). |
| | `last_execution` | TIMESTAMP | Última inferencia. |
| **RiskAlert** | `alert_id` | UUID | PK. |
| | `severity` | ENUM | LOW, MEDIUM, HIGH, CRITICAL. |
| | `message` | TEXT | Ej: "10 Quinquenios en 90 días". |
| | `financial_impact` | DECIMAL(15,2) | Cálculo de riesgo (Multa 30%). |
| | `is_dismissed` | BOOLEAN | Estado de atención. |

**Entidades: ESS y Flujos**
| Entidad | Campo | Tipo de Dato | Restricciones / Lógica |
| :--- | :--- | :--- | :--- |
| **SelfServiceAction** | `action_id` | UUID | PK. |
| | `person_id` | UUID | FK a Person. |
| | `action_type` | ENUM | DATA_UPDATE, LEAVE_REQ. |
| | `payload` | JSONB | Datos temporales pre-impacto. |
| **ApprovalWorkflow** | `workflow_id` | UUID | PK. |
| | `action_id` | UUID | FK. |
| | `current_step` | INT | Nivel de jerarquía. |
| | `history` | JSONB | **Invariante SoD**: Quién/Cuándo. |
| **Notification** | `notif_id` | UUID | PK. |
| | `recipient_id` | UUID | FK. |
| | `channel` | ENUM | PUSH_MOBILE, EMAIL. |
| | `read_at` | TIMESTAMP | Auditoría de notificación legal. |