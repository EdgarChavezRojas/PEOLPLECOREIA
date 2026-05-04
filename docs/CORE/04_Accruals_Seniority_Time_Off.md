# 04_Accruals_Seniority_Time_Off.md

## 4. BC: Accruals, Seniority & Time-Off (El "Reloj de Beneficios")
* **Dominios agrupados:** 11. Absences, Leaves & Holiday, 13. Seniority, Benefits & Accrual.
* **Por qué esta agrupación:** Dependen estrictamente del paso del tiempo y antigüedad continua (Vacaciones y Quinquenios).
* **Explicación técnica:** Gestiona todos los Saldos Acumulables. Permite compensar días de campo o calcular impacto de bajas en Bonos de Antigüedad.

---

## Agregados del Contexto

**Agregado 9: AccrualVault**
* **Root:** `AccrualBalance`. Libro contable de días o dinero acumulado.
* **Contenido:**
  * `SeniorityMilestone` (VO): Eventos fijos ("Cumplió 2 años") que disparan porcentajes.
  * `LeaveTransaction` (Entity): Solicitud de ausencia con identidad propia.

---

## Políticas Asociadas

**Clúster de Políticas: Compensación y Beneficios**
* **P1: Política de Base de Cálculo de Antigüedad:** ONGs/Universidades (1 SMN). Privadas/Comerciales (3 SMN - Bs 9.900). Se actualiza automáticamente.
* **P2: Política de Distribución de Utilidades:** Universidades exentas. Privadas provisionan 8,33% mensual para Primas.

**Clúster de Movilidad y Campo**
* **P11: Política de Viáticos y Misiones:** ONGs automatizan días compensatorios. Los viáticos no forman base imponible.

**Clúster de Acumulación de Beneficios**
* **P13: Política de Escala de Vacaciones:** 1-5 años (15 días), 5-10 años (20 días), >10 años (30 días). En educación permite "Receso Académico" masivo.
* **P14: Política de Mantenimiento de Valor (UFV):** Provisiona beneficios y RC-IVA actualizando a UFV.

---

## Workflows Orquestados

### 6. Workflow de Gestión de Ausencias y Vacaciones
* **Alcance:** Dominio 11, 8.
* **Agregados involucrados:** AccrualVault.
* **Descripción Técnica:** Desde la solicitud ESS hasta aprobación, validando saldos (P15) y feriados departamentales (P10).
* **Flujo:** Consulta Saldo (ESS) -> Configuración -> Validación Intersectorial Calendario -> Validación Saldo Invariante -> Notificación MSS -> Afectación del Vault irrevocable.

### 7. Workflow de Consolidación y Pago de Quinquenio
* **Alcance:** Dominios 13, 12, 8.
* **Agregados involucrados:** AccrualVault, EmploymentAgreement.
* **Descripción Técnica:** Automatiza elegibilidad (60 meses), cálculo de promedio (90 días) y pago.
* **Flujo:** Detección de Hito -> Solicitud ESS -> Cálculo Promedio -> Monitoreo Cronómetro Legal (Si T+30 días, multa 30% automática) -> Cierre y Asiento Contable (Preserva antigüedad).

---

## Eventos de Dominio

* **`LEAVE_REQUEST_SUBMITTED` (Sync):** Crea transacción Pending. Soft-Booking de días.
* **`LEAVE_REQUEST_MANAGER_APPROVED / REJECTED` (Async):** Aprobación MSS, notifica a Scheduling.
* **`VACATION_BALANCE_THRESHOLD_LOW` (Sync/Bloqueante):** Intercepta si días pedidos > saldo.
* **`ACCRUAL_BALANCE_DEDUCTED` (Async):** Hard-Deduction irreversible en el Vault.
* **`RANK_UPGRADE_ELIGIBILITY_REACHED` (Async):** Motor cronológico de Educación. Notifica a Comisión.
* **`QUINQUENIO_ELIGIBILITY_REACHED` (Async):** Habilita derecho de cobro a 60 meses. Notifica Finanzas.
* **`QUINQUENIO_REQUESTED` (Sync):** Inicia cronómetro de 30 días para pago.
* **`QUINQUENIO_CALCULATION_FINALIZED` (Async):** Calcula promedio de los últimos 3 meses (Total Ganado).
* **`QUINQUENIO_PAYMENT_OVERDUE` (Async):** Alerta crítica día 31. Aplica Multa 30% automáticamente.
* **`QUINQUENIO_PAYMENT_PROCESSED` (Sync):** Resetea contador en Vault, mantiene antigüedad.

---

## Diccionario de Datos

**Aggregate 9: AccrualVault**
| Entidad / VO | Campo | Tipo de Dato | Restricciones / Lógica |
| :--- | :--- | :--- | :--- |
| **AccrualBalance** (Root)| `balance_id` | UUID | PK. |
| | `relationship_id` | UUID | FK. |
| | `balance_type` | ENUM | VACATION, SENIORITY_BONUS. |
| | `unit` | ENUM | DAYS, AMOUNT_BOB. |
| | `current_balance` | DECIMAL(15,2) | **Invariante**: No saldo negativo. |
| | `last_accrual_date`| DATE | Fecha cálculo auto. |
| **LeaveTransaction** | `transaction_id` | UUID | PK. |
| | `balance_id` | UUID | FK. |
| | `start_date` | DATE | Fecha inicio. |
| | `end_date` | DATE | Fecha fin. |
| | `days_requested` | DECIMAL(5,2) | Días netos. |
| | `status` | ENUM | PENDING, APPROVED. |
| **SeniorityMilestone** | `milestone_id` | UUID | PK. |
| | `months_completed` | INT | Meses (24, 60). |
| | `base_smn_type` | ENUM | BASE_1_SMN, BASE_3_SMN. |

**Entidades Complementarias y Escalas**
| Entidad | Campo | Tipo de Dato | Restricciones / Lógica |
| :--- | :--- | :--- | :--- |
| **HolidayCalendar** | `holiday_id` | UUID | PK. |
| | `holiday_date` | DATE | Fecha feriado. |
| | `scope` | ENUM | NATIONAL, REGIONAL_SCZ. |
| **QuinquenioProvision**| `provision_id` | UUID | PK (8.33% mensual). |
| | `total_accumulated`| DECIMAL(15,2) | Monto proyectado (5 años). |
| | `penalty_active` | BOOLEAN | TRUE si excede 30 días. |
| **BenefitAccrual** | `benefit_id` | UUID | PK. |
| | `benefit_type` | ENUM | AGUINALDO, PRIMA_UTILIDAD. |
| | `accrued_amount` | DECIMAL(15,2) | Acumulado duodécimas. |
| **Seniority_Scale** | `scale_id` | UUID | PK. Tabla dinámica. |
| | `min_years/max_years`| INT | Límites inferior/superior. |
| | `vacation_days` | INT | Días de escala (15, 20, 30). |
| | `bonus_percentage` | DECIMAL(5,2) | % Bono Antigüedad. |