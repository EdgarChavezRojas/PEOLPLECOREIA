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

* **`ONBOARDING_COMPLETED` (Async):** Pilar 4 validado. Activa nómina/Scheduling y entrega equipos.
* **`OFFBOARDING_INITIATED` (Sync/Bloqueante):** Dispara cálculo P15 y P17, bloquea accesos y grilla.
* **`DATA_CHANGE_REQUESTED / REJECTED` (Async):** Modificación ESS pendiente de revisión.
* **`DISCIPLINARY_ACTION_REPORTED / MEMORANDUM_ISSUED` (Sync):** Crea PDF Inalterable y alerta sobre historial disciplinario.
* **`MEMORANDUM_ACKNOWLEDGED` (Sync):** Firma en ESS cierra notificación legal.
* **`DISCIPLINARY_THRESHOLD_REACHED` (Async):** Alerta IA (ej. 3 memorandos en 6 meses -> Despido Justificado).
* **`SUBSTITUTION_INITIATED / COMPLETED` (Sync):** Asigna TemporaryAssignment y calcula recargos.
* **`CERTIFICATE_REQUESTED / GENERATED / VALIDATED_EXTERNALLY` (Async):** Genera PDF/QR validable externamente si Kardex no está bloqueado.

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