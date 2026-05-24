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

**5. Contexto: Financial & Social (Presupuesto y Egresos)**

**Relación con el dinero, fuentes de financiamiento y entes externos.**

FUNDING\_SOURCE\_VALIDATED

- **Gatillo y Naturaleza (Sync): Motor de validación presupuestaria al intentar aprobar un contrato o adenda. Es Sincrónico y bloqueante.**
- **Lógica Funcional y Efectos: El sistema consulta el agregado BudgetAllocation. Valida que el ProjectID o DonorID tenga saldo suficiente para cubrir el costo total cargado (Haber Básico + Cargas Patronales + Provisiones).**
- **UI e IA:**
- **UI: Indicador visual de "Presupuesto Comprometido" en la ficha de contratación.**
- **IA: Analiza la tasa de quema (*Burn Rate*) del proyecto para alertar si el presupuesto alcanzará hasta el final del contrato pactado.**
- **Diseño para Localización: Obligatorio para el Tenant ONG/Fundaciones. En otros Tenants, puede ser opcional o simplificado a un centro de costos.**
- **Impacto en Invariantes: Consistencia del 100%: No se puede validar si la fuente de financiamiento no cubre la totalidad de la asignación.**

  ` `**FUNDING\_SOURCE\_PROJECT\_EXHAUSTED**

- **Gatillo y Naturaleza (Async): Disparado por el proceso de cierre de nómina o por el motor de ejecución presupuestaria.**
- **Lógica Funcional: Alerta cuando una partida llega al 0% de disponibilidad. Puede disparar el flujo de CONTRACT\_TERMINATED o solicitar una transferencia de fondos.**
- **UI e IA:**
- **IA: Genera una proyección de "fecha de agotamiento" basada en los incrementos salariales y el Bono de Antigüedad vegetativo.**
- **Localización: Crítico para la gestión de proyectos de cooperación internacional en Bolivia.**

  **COST\_CENTER\_SPLIT\_ADJUSTED**

- **Gatillo y Naturaleza (Sync): Modificación manual o por transferencia de sede (ORG\_UNIT\_ASSIGNED\_CHANGED).**
- **Lógica Funcional: Actualiza el LaborCostSplit. El sistema debe recalcular la distribución del costo del empleado entre diferentes unidades.**
- **UI e IA:**
- **UI: Gráfico de torta que valida visualmente que la suma de los splits sea exactamente el 100%.**
- **Localización: Vital en Retail para prorratear el sueldo de un cajero que trabajó 15 días en la sucursal "Norte" y 15 días en la sucursal "Equipetrol".**
- **RC\_IVA\_FORM\_110\_IMPORTED**
- **Gatillo y Naturaleza (Async): Importación masiva de archivos. dec o integración vía API con el SIAT (SIAT en Línea).**
- **Lógica Funcional: Registra el crédito fiscal presentado por el empleado. Impacta directamente en el cálculo del Sueldo Neto en la planilla tributaria.**
- **UI e IA:**
- **IA: Detecta anomalías en los montos presentados comparados con el promedio histórico del empleado para prevenir errores de carga.**
- **Localización: Aplica el procedimiento de la "Ley de Transparencia":**

$$\text{Impuesto Determinado} = (\text{Sueldo Neto} - 2 \times \text{SMN}) \times 0.13$$Posteriormente resta el 13% de 1 SMN y el Formulario 110.

**BANK\_ACCOUNT\_SYNCED**

- **Gatillo y Naturaleza (Async): Reacción al evento DATA\_CHANGE\_REQUESTED una vez validado el documento bancario.**
- **Lógica Funcional: Actualiza el destino de la dispersión de fondos para el archivo de transferencia bancaria (BISA, BNB, Mercantil, etc.).**
- **UI e IA:**
  - **UI: Check de "Cuenta Validada para Abono".**
- **Invariantes: Bloquea la generación del archivo de pago si la cuenta bancaria no ha pasado por el evento de sincronización/validación.**

  **LIQUIDATION\_CALCULATED**

- **Gatillo y Naturaleza (Sync): Disparado al iniciar el proceso de Offboarding.**
- **Lógica Funcional: Genera el borrador del "Finiquito". El sistema promedia el Total Ganado de los últimos 90 días y calcula duodécimas de Aguinaldo e Indemnización.**
- **UI e IA:**
- **IA: Sugiere si el motivo de retiro (ej. despido sin causa) requiere la inclusión del Desahucio (3 salarios promedio).**
- **Localización: Basado estrictamente en la P15 y P17 del motor legal boliviano.**
- **Impacto en Invariantes: La liquidación debe estar vinculada a un CONTRACT\_TERMINATED.**

**FINIQUITO\_PAYMENT\_OVERDUE**

**Gatillo y Naturaleza (Async): Cronómetro legal que se activa en $T+16$ días calendario después del cese.**

**Lógica Funcional: Alerta Crítica. Notifica que el plazo de 15 días para el pago ha vencido.**

**UI e IA:**

**UI: Alerta roja persistente en el dashboard de Payroll y Legal.**

Impacto en Invariantes: Aplica la Multa del 30% sobre el saldo líquido pagable:$$\text{Total a Pagar} = \text{Saldo Liquidación} \times 1.30$$
Localización: Implementación de la sanción por mora en beneficios sociales (Bolivia 2026).


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