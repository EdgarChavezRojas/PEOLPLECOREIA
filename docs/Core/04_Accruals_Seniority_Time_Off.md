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

**4. Contexto: Accruals & Time-Off (Reloj Legal de Beneficios)**

**Eventos de acumulación de derechos por el paso del tiempo y solicitudes de ausencia.**

**LEAVE\_REQUEST\_SUBMITTED**

- **Gatillo y Naturaleza (Sync): Acción manual del colaborador desde el ESS (Self-Service). Es Sincrónico para validar disponibilidad inmediata en el AccrualVault antes de permitir el envío.**
- **Lógica Funcional y Efectos: Crea una LeaveTransaction en estado Pending. El sistema realiza una "Reserva" (Soft-Booking) de días en el saldo para evitar solicitudes duplicadas que excedan el cupo.**
- **UI e IA:**
  - **UI: Calendario interactivo con feriados de Santa Cruz resaltados (24 de septiembre).**
  - **IA: Detecta si la fecha solicitada coincide con picos históricos de rotación o demanda en Retail, sugiriendo al supervisor la viabilidad de la aprobación.**
- **Localización: Valida automáticamente si el tipo de permiso (maternidad, duelo, nupcias) requiere carga de evidencia en el Digital Kardex.**
- **Invariantes: No permite solicitudes en fechas pasadas sin un rol de "Admin Override" con auditoría.**

**LEAVE\_REQUEST\_MANAGER\_APPROVED / REJECTED**

- **Gatillo y Naturaleza (Async): Acción del supervisor vía MSS (Manager Self-Service). Es Asincrónico para permitir la orquestación de notificaciones.**
- **Lógica Funcional:**
  - **Approved: Cambia el estado de la transacción a Approved. Si la fecha de inicio es menor a 48h, notifica a Scheduling para buscar un suplente.**
  - **Rejected: Libera la "Reserva" (Soft-Booking) en el vault.**
- **UI e IA:**
  - **UI: Notificación Push inmediata al empleado.**
  - **IA: Aprende los motivos de rechazo comunes del manager para optimizar futuras solicitudes.**
- **Invariantes: Un manager no puede aprobar su propia solicitud (Segregación de Funciones).**

**VACATION\_BALANCE\_THRESHOLD\_LOW**

- **Gatillo y Naturaleza (Sync): Motor de reglas del AccrualVault durante la solicitud.**
- **Lógica Funcional: Intercepta la solicitud si Saldo\_Actual - Días\_Solicitados < 0.**
- **UI e IA:**
  - **UI: Bloqueo del botón "Enviar" con tooltip: "Días insuficientes". En ONGs, puede permitir saldo negativo si existe una política de "Anticipo de Vacaciones" vinculada al proyecto.**
  - **IA: Estima en cuántos meses el empleado recuperará el saldo necesario basado en su tasa de devengamiento (15, 20 o 30 días según antigüedad).**

**ACCRUAL\_BALANCE\_DEDUCTED**

- **Gatillo y Naturaleza (Async): Registro contable final tras la aprobación de la ausencia o el cierre del periodo.**
- **Lógica Funcional: Impacta el AccrualVault de forma definitiva (Hard-Deduction). Genera un asiento de auditoría inalterable.**
- **Impacto en Invariantes: Garantiza que la suma de todos los movimientos de salida coincida con la reducción del saldo maestro.**

**Seniority Milestones (Quinquenios y Escalafón)**

` `**RANK\_UPGRADE\_ELIGIBILITY\_REACHED**

- **Gatillo y Naturaleza (Async): Motor cronológico de Educación.**
- **Lógica Funcional: El sistema detecta que el docente cumplió los años requeridos en su categoría actual. Notifica a la Comisión Académica.**
- **UI e IA:**
  - **UI: Alerta en el dashboard de RRHH: "Docente elegible para ascenso de escalafón".**
  - **IA: Analiza si el docente tiene los certificados de postgrado validados en el Kardex para confirmar el ascenso automático.**
- **Invariantes: El cambio de rango no ocurre hasta que se registre el evento ACADEMIC\_PROFILE\_RANK\_UPDATED.**

**QUINQUENIO\_ELIGIBILITY\_REACHED**

- **Gatillo y Naturaleza (Async): Disparado al cumplir 60 meses de antigüedad ininterrumpida.**
- **Lógica Funcional: Habilita el derecho al cobro de la indemnización acumulada. Notifica proactivamente a Finanzas para la provisión de fondos.**
- **Localización: En Bolivia, el quinquenio es un derecho consolidado a los 5 años, independientemente de si el empleado continúa trabajando.**
- **Impacto en Invariantes: Inderogabilidad: El sistema no permite "resetear" la antigüedad para el Bono de Antigüedad, solo para la bolsa de indemnización.**

**QUINQUENIO\_REQUESTED**

- **Gatillo y Naturaleza (Sync): Firma digital de la solicitud por el empleado en el ESS.**
- **Lógica Funcional: Inicia el cronómetro legal de 30 días calendario para el pago (P8). Bloquea cualquier modificación retroactiva en el salario del último trimestre.**
- **Invariantes: Solo se puede solicitar si la antigüedad es un múltiplo exacto de 60 meses o superior.**

**QUINQUENIO\_CALCULATION\_FINALIZED**

- **Gatillo y Naturaleza (Async): Motor de Payroll.**
- **Lógica Funcional: Calcula el promedio del "Total Ganado" de los últimos 3 meses (90 días).**
- **UI e IA:**
  - **IA: Identifica si hubo incrementos salariales atípicos en los últimos 3 meses que pudieran inflar el quinquenio (Riesgo de fraude interno).**
- **Invariantes: Base Inviolable: El cálculo debe incluir todas las variables (comisiones, recargos) percibidas en el trimestre anterior.**

**QUINQUENIO\_PAYMENT\_OVERDUE**

- **Gatillo y Naturaleza (Async): Alerta crítica al cumplir 31 días desde la solicitud sin registro de pago.**
- **Lógica Funcional: Aplica automáticamente la Multa del 30% sobre el monto total de la indemnización, según normativa boliviana.**
- **UI e IA:**
  - **UI: Alerta roja "Nivel Crítico" enviada a la Gerencia General y Legal.**
- **Invariantes: El sistema no permite ignorar la multa una vez disparada, asegurando cumplimiento legal estricto.**

**QUINQUENIO\_PAYMENT\_PROCESSED**

- **Gatillo y Naturaleza (Sync): Registro del comprobante de transferencia o cheque.**
- **Lógica Funcional: Resetea el contador de quinquenios en el AccrualVault. Archiva la liquidación como evidencia legal.**
- **Impacto en Invariantes: El pago de quinquenio no rompe la continuidad laboral; la fecha de ingreso original se mantiene inalterable para el escalafón y las vacaciones.**


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