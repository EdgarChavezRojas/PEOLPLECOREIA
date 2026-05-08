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

**Eventos que dictan las reglas de juego para Tiempo, Nómina y Auditoría Legal.**

**CONTRACT\_DRAFTED**

**Registro del objeto legal en estado latente.**

- **Gatillo y Naturaleza (Sync): Acción manual del Analista de RRHH o automatismo del módulo de Reclutamiento (ATS). Es Sincrónico para validar que la Position esté vacante antes de permitir el borrador.**
- **Lógica Funcional y Efectos Colaterales: Crea un Contract en estado Draft. Bloquea la Position para que no se asigne a otro candidato. El módulo de Budget Control recibe una reserva preventiva de fondos (Pre-encumbrance).**
- **UI e IA:**
  - **UI: Indicador de "Posición Reservada".**
  - **IA: El sistema analiza si el sueldo propuesto está alineado con la banda salarial (Grade/Band) del cargo para evitar inequidades internas.**
- **Localización: Se parametriza el tipo de contrato (Indefinido, Plazo Fijo, Obra). En ONGs, se exige el ID del Proyecto/Donante como metadato obligatorio.**
- **Impacto en Invariantes: Protege la "Control de Plazas": No se puede draftear un contrato si el *headcount* está al 100%.**

**CONTRACT\_APPROVED**

**Formalización del vínculo tras la Segregación de Funciones (SoD).**

- **Gatillo y Naturaleza (Async): Aprobación por un usuario de mayor jerarquía (Gerente de RRHH o Finanzas). Es Asincrónico para la cadena de notificaciones, pero Sincrónico para la persistencia del estado.**
- **Lógica Funcional: Cambia el estado a Approved. Genera el DocumentRecord en el Digital Kardex. Si la effective\_from es hoy, activa automáticamente la Relationship a Active.**
- **UI e IA:**
  - **UI: Check de validación verde. Generación automática del PDF con firma digital o QR de validación.**
  - **IA: Registra el tiempo de ciclo desde el borrador hasta la aprobación para KPIs de eficiencia de contratación.**
- **Diseño para Localización: Si el empleado está en Santa Cruz y el Tenant es Corporativo, se inyecta la regla del Aporte INFOCAL (1%) en el motor de nómina para este contrato.**
- **Impacto en Invariantes: Cumple con la "Segregación de Funciones": Un contrato no puede ser aprobado por la misma persona que lo drafteó.**

` `**CONTRACT\_LEGAL\_PISO\_VIOLATED**

**Salvaguarda contra la ilegalidad salarial.**

- **Gatillo y Naturaleza (Sync): Motor de reglas de cumplimiento (Compliance Engine). Es Sincrónico y Bloqueante.**
- **Lógica Funcional: Intercepta cualquier intento de guardar un salario menor al SMN vigente ($Bs. 3.300$). Detiene la transacción y registra un log de intento de violación normativa.**
- **UI e IA:**
  - **UI: Banner de error crítico: "Violación de Ley: El haber básico no puede ser inferior a Bs. 3.300".**
  - **IA: Genera un insight de riesgo legal para el Tenant, alertando sobre posibles multas administrativas.**
- **Diseño para Localización: El valor del SMN ($Bs. 3.300$) es una variable global versionada. Si el gobierno decreta un incremento, se actualiza la política y el evento reacciona al nuevo umbral.**
- **Impacto en Invariantes: Protege el "Piso Salarial Legal".**

**CONTRACT\_TÁCITA\_RECONDUCCIÓN\_RISK**

**Alerta preventiva de conversión de contrato.**

- **Gatillo y Naturaleza (Async): Proceso programado (Cron Job) que escanea contratos de Plazo Fijo.**
- **Lógica Funcional: Se dispara a los $T-90$ días de la fecha de vencimiento. Envía alertas al supervisor y a RRHH.**
- **UI e IA:**
  - **UI: Widget en el Dashboard: "Contratos con riesgo de reconducción".**
  - **IA: Predice el impacto financiero si el contrato se vuelve indefinido (Cálculo de provisión de indemnización por años de servicio).**
- **Diseño para Localización: Basado en el Art. 12 de la LGT boliviana. El sistema diferencia si es el primer o segundo contrato a plazo fijo.**
- **Impacto en Invariantes: Mantiene la "Integridad Temporal": Evita que el sistema mantenga contratos vencidos activos sin adenda.**

**MAX\_RENEWALS\_REACHED**

**Bloqueo de fraude a la ley por renovaciones sucesivas.**

- **Gatillo y Naturaleza (Sync): Motor de reglas al intentar crear una nueva adenda de prórroga. Sincrónico y Bloqueante.**
- **Lógica Funcional: Si el contrato ha llegado a 2 renovaciones de plazo fijo (límite en Bolivia), bloquea cualquier opción que no sea "Conversión a Indefinido" o "Terminación".**
- **UI e IA:**
  - **UI: Opción de "Extender Plazo Fijo" deshabilitada con tooltip explicativo sobre el límite legal.**
- **Localización: Implementación estricta de la jurisprudencia boliviana sobre fraude a la ley en contratos sucesivos.**
- **Impacto en Invariantes: Garantiza que la "Naturaleza del Vínculo" sea legalmente coherente.**

**ADENDUM\_APPROVAL\_REQUIRED**

**Control de cambios en las condiciones pactadas.**

- **Gatillo y Naturaleza (Sync): Modificación de salario, cargo o jornada en un contrato activo.**
- **Lógica Funcional: Crea una versión Pending de la adenda. Mantiene la versión actual del contrato como Active hasta que la adenda sea aprobada.**
- **UI e IA:**
  - **UI: Comparativa visual "Lado a Lado" (Antes vs. Después) para el aprobador.**
- **Diseño para Localización: Si el cambio es de Santa Cruz a otra ciudad, alerta sobre cambios en feriados regionales y aportes patronales específicos.**
- **Impacto en Invariantes: Soporta el "Effective Dating": Los cambios no sobreescriben, crean una nueva línea de tiempo.**

**ADDENDUM\_SALARY\_ADJUSTMENT\_APPROVED**

**Impacto financiero y recalibración de beneficios.**

- **Gatillo y Naturaleza (Async): Aprobación final de la adenda salarial.**
- **Lógica Funcional: Actualiza el haber básico. Notifica al módulo de Seniority & Benefits para recalcular el Bono de Antigüedad (especialmente si el SMN cambió).**
- **UI e IA:**
  - **IA: Calcula la "Deriva Salarial": ¿Cuánto aumentó el costo total de la planilla con este ajuste?**
- **Diseño para Localización: Recalcula el RC-IVA bajo la "Ley de Transparencia" (13% real) para mostrar al empleado su nuevo sueldo neto estimado.**
- **Impacto en Invariantes: Actualiza la "Base de Cálculo Inviolable" para futuras indemnizaciones.**

**CONTRACT\_TERMINATED**

**Cierre de obligaciones y activación de liquidación.**

- **Gatillo y Naturaleza (Sync/Async): Registro de baja (Renuncia, Despido, Fin de Contrato).**
- **Lógica Funcional: Cambia el estado a Terminated. Notifica a Social Security & Tax para la baja en la Gestora/Caja. Dispara el workflow de Finiquito con un cronómetro de 15 días.**
- **UI e IA:**
  - **UI: Checklist de "Offboarding" (Entrega de activos, firma de finiquito).**
  - **IA: Análisis de causa raíz de la baja (Entrevista de salida digital) para detectar fugas de talento.**
- **Diseño para Localización: Calcula automáticamente si corresponde Desahucio (3 salarios) en caso de despido injustificado según ley boliviana.**
- **Impacto en Invariantes: Libera la Position y desactiva la Relationship para evitar pagos en el siguiente ciclo de nómina.**


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