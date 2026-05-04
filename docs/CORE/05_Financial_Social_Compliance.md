# 05_Financial_Social_Compliance.md

## 5. BC: Financial & Social Compliance (El "Módulo de Salidas")
* **Dominios agrupados:** 10. Budget Allocation & Funding Control, 14. Social Security & Regulatory Relations.
* **Por qué esta agrupación:** Ambos manejan distribución del costo laboral a entes externos (Donantes, Impuestos, Gestora).
* **Explicación técnica:** Asegura Imputación Analítica exacta. Ajusta el presupuesto (P&L) y la previsión del aporte patronal.

---

## Agregados del Contexto

**Agregado 10: BudgetFunding**
* **Root:** `FundingSource`. Representa la fuente del dinero (Proyecto). Sin esto no hay imputación.
* **Contenido:**
  * `LaborCostSplit` (VO): Distribución porcentual que debe sumar 100%.

---

## Políticas Asociadas

**Clúster de Territorialidad: Santa Cruz**
* **P9: Política de Aporte Patronal INFOCAL Santa Cruz:** 1% comercial/privado, desactivado en ONGs.
* **P10: Política de Feriados Regionales:** Automatiza el 24 de septiembre y recargos.

**Clúster Fiscal y de Liquidación**
* **P15: Política de Base de Cálculo (Promedio Indemnizable):** Promedio de últimos 3 meses (Total Ganado) para Aguinaldos/Finiquitos. Retail incluye comisiones.
* **P16: Política de Aguinaldo:** Provisión (8,33%) y pago doble si pasa del 20/Dic.
* **P17: Política de Finiquito y Desahucio:** Plazo máximo 15 días (multa 30%). Añade 3 salarios si hay Despido sin Causa.
* **P18: Política "IVA Transparente" (RC-IVA 13%):** Deducción Sueldo Neto - 2 SMN al 13%. Resta 13% de 1 SMN.

---

## Workflows Orquestados

### 9. Workflow de Offboarding y Liquidación (Finiquito)
* **Alcance:** Dominios 3, 5, 14.
* **Agregados involucrados:** EmploymentRelationship, EmploymentAgreement, AccrualVault.
* **Descripción Técnica:** Extingue el vínculo laboral garantizando pago exacto en 15 días.
* **Flujo:** Desvinculación -> Validación Candado Activos -> Cálculo Promedio (P15) -> Liquidación Conceptos (P17 Desahucio) -> Revisión RC-IVA (UFV) -> Cronómetro 15 días (Si mora, multa 30% automática).

### 13. Workflow de Suplencias y Reemplazos Temporales
* **Alcance:** Dominios Retail/Universidades (P&L).
* **Propósito:** Asigna funciones/recargos al suplente sin contrato indefinido adicional.
* **Flujo:** Identificación (Skillset) -> Validación Jornada Legal (48h/40h) -> Recargo por Suplencia -> Temporary Assignment -> Imputación de Costo al CostCenter del titular.

---

## Eventos de Dominio

* **`FUNDING_SOURCE_VALIDATED` (Sync/Bloqueante):** Verifica que ProjectID tenga saldo para sueldo/cargas antes de aprobar contratos.
* **`FUNDING_SOURCE_PROJECT_EXHAUSTED` (Async):** Alerta cuando la partida llega al 0%.
* **`COST_CENTER_SPLIT_ADJUSTED` (Sync):** Recalcula LaborCostSplit. Invariante 100%.
* **`RC_IVA_FORM_110_IMPORTED` (Async):** Importa facturas (SIAT) e impacta Sueldo Neto en la planilla.
* **`BANK_ACCOUNT_SYNCED` (Async):** Actualiza el destino de transferencia bancaria y desbloquea el archivo de dispersión.
* **`LIQUIDATION_CALCULATED` (Sync):** Genera borrador del Finiquito con promedio últimos 90 días.
* **`FINIQUITO_PAYMENT_OVERDUE` (Async):** Cronómetro T+16 días activa Multa del 30% sobre el saldo pagable.

---

## Diccionario de Datos

**Aggregate 10: BudgetFunding**
| Entidad / VO | Campo | Tipo de Dato | Restricciones / Lógica |
| :--- | :--- | :--- | :--- |
| **FundingSource** (Root) | `source_id` | UUID | PK. |
| | `project_code` | VARCHAR(50) | **Obligatorio ONGs**. |
| | `total_budget` | DECIMAL(18,2) | Presupuesto total. |
| | `available_budget`| DECIMAL(18,2) | Saldo actual. |
| | `burn_rate` | DECIMAL(5,2) | Tasa de consumo (IA). |
| **LaborCostSplit** | `split_id` | UUID | PK. |
| | `unit_id` | UUID | FK a OrgUnit. |
| | `percentage` | DECIMAL(5,2) | **Invariante**: Suma 100%. |
| | `effective_date` | DATE | Fecha de imputación. |

**Entidades: Seguridad Social y Tributaria**
| Entidad | Campo | Tipo de Dato | Restricciones / Lógica |
| :--- | :--- | :--- | :--- |
| **SocialSecurityAccount**| `ssa_id` | UUID | PK. |
| | `gestora_code` | VARCHAR(30) | NUA/CUA. |
| | `contribution_rate`| DECIMAL(5,4) | **Fijo**: 12,71%. |
| | `last_contribution`| DATE | Fecha último aporte. |
| **HealthProvider** | `provider_id` | UUID | PK (Caja Nacional, etc.). |
| | `registration_no` | VARCHAR(50) | Número afiliación. |
| | `status` | ENUM | Activo, Suspendido. |
| **TaxForm110** | `form_id` | UUID | PK. |
| | `total_declared` | DECIMAL(15,2) | Total facturas. |
| | `verified_credit` | DECIMAL(15,2) | 13% del declarado. |
| | `doc_id` | UUID | FK a Kardex. |