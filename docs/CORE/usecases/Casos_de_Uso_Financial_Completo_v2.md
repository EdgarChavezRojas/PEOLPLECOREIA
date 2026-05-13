# Documentación Integral: Casos de Uso del Bounded Context Financial & Social Compliance

Este documento describe la totalidad de los 12 casos de uso que componen el núcleo del motor financiero y de cumplimiento social. Se detalla su propósito, la lógica de negocio aplicada, los disparadores (triggers) y cómo se conectan con el resto del ecosistema (Payroll, IAM, Reclutamiento).

---

## 1. Casos de Uso de Cumplimiento Normativo (Nuevos)

### 1.1 ProcessQuinquenioPaymentUseCase
* **Propósito**: Gestionar el pago de la indemnización consolidada por cada 5 años de servicio continuo sin que medie desvinculación laboral.
* **Circunstancia**: Solicitud voluntaria del empleado a través del portal ESS (Employee Self Service) tras cumplir el hito de 60 meses.
* **Conexión**:
    * **Trigger**: Acción humana (Empleado).
    * **Flujo**: Valida la antigüedad en el módulo de *Employment* -> Calcula el promedio de los últimos 3 meses -> Genera un pago exento de aportes de ley.
    * **Salida**: Emite `QuinquenioRequestedEvent` para que Payroll lo procese y Tesorería realice la dispersión.

### 1.2 ApplyUfvMaintenanceUseCase
* **Propósito**: Mantener el valor adquisitivo de los saldos de crédito fiscal (RC-IVA) y las provisiones de beneficios sociales mediante la indexación a la UFV.
* **Circunstancia**: Ejecución diaria obligatoria para asegurar la precisión de los saldos.
* **Conexión**:
    * **Trigger**: Cron Job (Sincronización diaria 00:01 AM).
    * **Flujo**: Consume el valor de la UFV vía Web Scraping desde el BCB -> Calcula el factor de actualización -> Impacta en las tablas de saldos.
    * **Salida**: Actualiza estados financieros y emite `UfvMaintenanceAppliedEvent`.

### 1.3 CalculateAnnualAguinaldoUseCase
* **Propósito**: Cálculo masivo del Aguinaldo de Navidad (un sueldo completo o duodécimas).
* **Circunstancia**: Mes de diciembre de cada gestión fiscal.
* **Conexión**:
    * **Trigger**: Tiempo (Programado).
    * **Lógica P16**: Si se paga después del 20 de diciembre, el sistema aplica automáticamente la penalidad de pago doble.
    * **Salida**: Genera la planilla de aguinaldos enviando los montos a Payroll.

### 1.4 EvaluatePrimaAnnualUseCase
* **Propósito**: Distribución del beneficio de Primas de Utilidades basado en el rendimiento económico de la empresa.
* **Circunstancia**: Cierre de gestión fiscal (post-balance de resultados).
* **Conexión**:
    * **Trigger**: Evento financiero (Carga de Utilidad Neta).
    * **Lógica P2**: Excluye automáticamente a ONGs y entidades educativas. Prorratea el 25% de la utilidad si esta no alcanza para cubrir un sueldo por empleado.
    * **Salida**: Emite `PrimaCalculatedEvent`.

---

## 2. Casos de Uso de Cálculo Mensual y Beneficios (Existentes)

### 2.1 CalculateRcIvaUseCase
* **Propósito**: Determinación del impuesto sobre los ingresos del personal dependiente.
* **Conexión**: Se dispara cada vez que se procesa la nómina mensual. Recibe el sueldo neto y resta el crédito fiscal acumulado.

### 2.2 EvaluateSegundoAguinaldoUseCase
* **Propósito**: Evaluar la obligatoriedad del pago del "Segundo Aguinaldo" basado en el crecimiento del PIB.
* **Lógica P19**: Funciona como un *Feature Toggle*. Solo se activa si el Decreto Supremo anual confirma un crecimiento superior al 4.5%.

### 2.3 ImportTaxForm110UseCase
* **Propósito**: Procesar la presentación de facturas de los empleados para descargar el RC-IVA.
* **Conexión**: El empleado carga el archivo; el caso de uso valida los montos y actualiza el saldo de crédito fiscal para el siguiente ciclo de nómina.

### 2.4 ImputeAnalyticTerritorialUseCase
* **Propósito**: Prorratear los costos laborales (Sueldo, Aportes, Provisiones) entre diferentes centros de costo o sedes geográficas.
* **Conexión**: Vital para la contabilidad analítica y el reporte por proyectos (especialmente en ONGs).

---

## 3. Casos de Uso de Ciclo de Vida y Estructura

### 3.1 ProcessLiquidationUseCase
* **Propósito**: Ejecución del Workflow 9 (Finiquito). Calcula indemnizaciones, desahucio, aguinaldos y vacaciones pendientes.
* **Circunstancia**: Desvinculación (Retiro forzoso o renuncia).
* **Conexión**: Inicia el plazo de 15 días calendario para el pago legal antes de la multa del 30%.

### 3.2 RegisterSocialSecurityUseCase
* **Propósito**: Automatizar el alta del empleado en la Gestora Pública y la Caja de Salud correspondiente.
* **Conexión**: Reacciona al evento `EmployeeHiredEvent` del módulo de contratación.

### 3.3 SyncBankAccountUseCase
* **Propósito**: Validar y sincronizar las cuentas bancarias para asegurar que la dispersión de fondos (pagos) sea exitosa.
* **Conexión**: Interfaz directa con los puertos de comunicación bancaria.

### 3.4 ValidateFundingSourceUseCase
* **Propósito**: Verificar que exista presupuesto disponible en la fuente de financiamiento antes de comprometer un gasto laboral.
* **Circunstancia**: Crítico para ONGs.
* **Conexión**: Bloquea el proceso de contratación o pago si la fuente (Donante) no tiene saldo.

---

## Matriz de Interacción Modular

| Módulo Origen | Acción / Evento | Caso de Uso Destino |
| :--- | :--- | :--- |
| **Employment** | Nuevo Ingreso | `RegisterSocialSecurityUseCase` |
| **IAM / ESS** | Solicitud Empleado | `ProcessQuinquenioPaymentUseCase` |
| **Time Management** | Cierre de Vacaciones | `ProcessLiquidationUseCase` |
| **External (BCB)** | Cambio de UFV | `ApplyUfvMaintenanceUseCase` |
| **Financial (ONG)** | Carga de Fondos | `ValidateFundingSourceUseCase` |

---
