# 02_Employment_Terms_Regulatory_Compliance.md

## 2. BC: Employment Terms & Regulatory Compliance (El "Cerebro Legal")
* **Dominios agrupados:** 5. Contracts & Legal Terms, 9. Compliance & Policy Engine, 12. Workflow, Audit & Legal Evidence.
* **Por qué esta agrupación:** Un contrato en Bolivia es un conjunto de reglas legales vivas. Unir el Contrato con el Motor de Cumplimiento valida en tiempo real que el sueldo pactado no sea menor al SMN antes de guardar.
* **Explicación técnica:** El motor de cumplimiento actúa como interceptor. Toda adenda genera evidencia de auditoría y eventos SoD para defensa legal.

---

## Agregados del Contexto

**Agregado 5: EmploymentAgreement**
* **Root:** `Contract`. Entidad legal máxima (reglas de pago y jornada).
* **Contenido:**
  * `ContractAddendum` (Entity): Tienen fecha de firma propia (Effective Dating).
  * `SalaryTerms` (VO): Cualquier cambio salarial genera nuevo registro VO.
  * `ComplianceSnapshot` (VO): Captura qué leyes estaban vigentes (ej. SMN Bs 3.300) al firmar.

**Agregado 6: CompliancePolicy**
* **Root:** `PolicyRule`. Define la regla abstracta que el sistema ejecuta.
* **Contenido:**
  * `LegalThreshold` (VO): Valores inmutables (Bs 3.300, 12.71%). Si cambia, se crea nuevo umbral.

---

## Políticas Asociadas
**Clúster de Políticas: Jornada y Tiempo Legal**
* **P5: Política de Límite de Jornada por Género:** Bloquea planificación > 48h (varones) y 40h (mujeres/menores). Emite advertencia o reclasifica excedente a horas extras.
* **P6: Política de Recargo Nocturno y Dominical:** Automatiza recargos de 20:00 a 06:00 (Retail) y compensación/recargo dominical del 100%.

**Clúster de Protección Contractual**
* **P12: Política de Prevención de Tácita Reconducción:** Emite alertas a los 90 días antes del vencimiento de contratos a plazo fijo para evitar indemnizaciones por omisión (Art. 12 LGT).

---

## Workflows Orquestados

### 2. Workflow de Modificación Contractual (Adendas)
* **Alcance:** Dominios 5, 9, 12.
* **Agregados involucrados:** EmploymentAgreement, CompliancePolicy.
* **Descripción Técnica:** Gestiona cualquier cambio en sueldo, cargo o jornada cumpliendo con el motor de políticas (SMN, RC-IVA).
* **Flujo:** Solicitud -> Interceptor de Cumplimiento (P1, P13, Piso Salarial) -> Control SoD (Invariante) -> Bifurcación (>15% salarial o cambio de cargo) -> Firma y Aplicación (Effective Dating).

### 12. Workflow de Gestión Disciplinaria y Memorandos
* **Alcance:** Legal / Compliance (Impacta Kardex).
* **Propósito:** Registrar llamadas de atención, sanciones o memorandos.
* **Flujo:** Reporte de Incidente -> Clasificación (Leve/Grave/Mérito) -> Carga Evidencia -> Generación PDF Inalterable -> Notificación y Firma Digital (ESS) -> Evaluación de Reincidencia (Dispara alerta de despido justificado).

---

## Eventos de Dominio

* **`CONTRACT_DRAFTED` (Sync):** Crea contrato en estado latente. Bloquea la Position y reserva fondos.
* **`CONTRACT_APPROVED` (Async):** Formaliza el vínculo tras SoD. Genera DocumentRecord.
* **`CONTRACT_LEGAL_PISO_VIOLATED` (Sync/Bloqueante):** Intercepta salario < Bs 3.300.
* **`CONTRACT_TÁCITA_RECONDUCCIÓN_RISK` (Async):** Disparado a los T-90 días del vencimiento del plazo fijo.
* **`MAX_RENEWALS_REACHED` (Sync/Bloqueante):** Bloquea si contrato llega a 2 renovaciones de plazo fijo.
* **`ADENDUM_APPROVAL_REQUIRED` (Sync):** Crea adenda Pending, mantiene actual Active.
* **`ADDENDUM_SALARY_ADJUSTMENT_APPROVED` (Async):** Actualiza básico, recalcula Bono de Antigüedad y RC-IVA proyectado.
* **`CONTRACT_TERMINATED` (Sync/Async):** Cambia a Terminated, notifica bajas y dispara Finiquito.

---

## Diccionario de Datos

**Aggregate 5: EmploymentAgreement**
| Entidad / VO | Campo | Tipo de Dato | Restricciones / Lógica |
| :--- | :--- | :--- | :--- |
| **Contract** (Root) | `contract_id` | UUID | PK. |
| | `relationship_id` | UUID | FK a Relationship. |
| | `contract_type` | ENUM | Indefinido, Plazo Fijo, Obra. |
| | `status` | ENUM | Draft, Approved, Terminated. |
| | `project_id` | VARCHAR(50) | **Obligatorio para ONGs**. |
| **ContractAddendum** | `addendum_id` | UUID | PK. |
| | `contract_id` | UUID | FK. |
| | `effective_from` | DATE | Fecha de vigencia de modificación. |
| | `effective_to` | DATE | Fecha fin. |
| **SalaryTerms** | `basic_salary` | DECIMAL(15,2)| **Invariante**: >= Bs 3.300. |
| | `total_earned_proj`| DECIMAL(15,2)| Proyección Total Ganado. |
| | `net_salary_proj` | DECIMAL(15,2)| Proyección neta. |
| | `currency` | VARCHAR(3) | ISO (BOB). |
| **ComplianceSnapshot**| `smn_applied` | DECIMAL(15,2)| SMN al firmar. |
| | `tax_regime` | VARCHAR(50) | Ley RC-IVA. |
| | `infocal_active` | BOOLEAN | Switch regional. |

**Aggregate 6: CompliancePolicy**
| Entidad / VO | Campo | Tipo de Dato | Restricciones / Lógica |
| :--- | :--- | :--- | :--- |
| **PolicyRule** (Root) | `policy_id` | UUID | PK. |
| | `policy_name` | VARCHAR(100) | Nombre de regla. |
| | `description` | TEXT | Explicación legal. |
| **LegalThreshold** | `threshold_value` | DECIMAL(15,4)| Valores inmutables. |
| | `effective_date` | DATE | Fecha de vigencia. |
| **EligibilityMatrix** | `matrix_id` | UUID | PK. |
| | `tenant_type` | ENUM | Retail, ONG, etc. |
| | `job_code` | VARCHAR(20) | FK a Job. |
| | `req_documents` | JSONB | Lista de documentos (P7). |

**Entidades Transversales y de Auditoría**
| Entidad | Campo | Tipo de Dato | Restricciones / Lógica |
| :--- | :--- | :--- | :--- |
| **AuditLog** | `audit_id` | UUID | PK. Registro WORM. |
| | `entity_name` | VARCHAR(50) | Entidad afectada. |
| | `diff_data` | JSONB | Historial "antes" y "después". |
| | `timestamp` | TIMESTAMP | Hora del servidor. |
| **WorkflowInstance** | `workflow_id` | UUID | PK. |
| | `creator_user_id`| UUID | Creador. |
| | `approver_user_id`| UUID | **Invariante SoD**: != creador. |
| **ElectronicEvidence**| `evidence_id` | UUID | PK. |
| | `hash_sha256` | VARCHAR(64) | Hash para RAG/Inalterabilidad. |
| | `qr_code_url` | VARCHAR(255) | URL de validación Zero-Trust. |