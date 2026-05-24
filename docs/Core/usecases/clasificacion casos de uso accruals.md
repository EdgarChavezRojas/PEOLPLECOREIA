# Matriz de Puntos de Entrada de Casos de Uso (Accruals & Time-Off)

Este documento define la topología de invocación para cada uno de los Casos de Uso (Use Cases) de la capa de aplicación del módulo de Accruals.

## Clasificación de Casos de Uso

| **Caso de Uso (UseCase)** | **Tipo de Invocación** | **Actor / Disparador (Trigger)** | **Flujo de Negocio Relacionado** |
| :--- | :--- | :--- | :--- |
| **`RequestLeaveUseCase`** | REST Controller (API) | Empleado (vía Employee Self-Service - ESS) | **Workflow 6:** Gestión de Ausencias y Vacaciones. Inicia la solicitud de vacación o permiso. |
| **`ApproveLeaveUseCase`** | REST Controller (API) | Supervisor / Manager (vía Manager Self-Service - MSS) | **Workflow 6:** Deduce el saldo real en el Vault tras la aprobación del jefe directo. |
| **`RejectLeaveUseCase`** | REST Controller (API) | Supervisor / Manager (vía MSS) | **Workflow 6:** Libera la reserva (*Soft-Booking*) de días solicitados. |
| **`OpenAccrualBalanceUseCase`** | Event-Driven (Consumer) | Evento de Integración: `RELATIONSHIP_CREATED` o `ONBOARDING_COMPLETED` | **Workflow 1:** Onboarding Integral. Un *Listener* intercepta el alta del empleado e inicializa sus saldos de beneficios en 0. |
| **`ProvisionBenefitBatchUseCase`**| Scheduled (Batch) | Scheduler de Infraestructura (`BenefitProvisionScheduler`) a fin de mes. | **P16:** Política de Aguinaldo y Duodécimas. Ejecuta la provisión mensual masiva para empleados activos. |
| **`AccrueVacationUseCase`** | Scheduled (Batch) / Event | Cron Job mensual o Evento de "Aniversario Laboral" | **P13:** Política de Escala de Vacaciones. Inyecta los 15, 20 o 30 días al saldo disponible según años de servicio. |
| **`RegisterSeniorityMilestoneUseCase`**| Scheduled (Batch) / Event | Proceso de cálculo de antigüedad (evaluando los meses de servicio) | **P1:** Política de Base de Antigüedad. Genera los hitos biográficos que escalan el porcentaje del Bono de Antigüedad. |
| **`ProvisionQuinquenioUseCase`** | Scheduled (Batch) | Proceso de cierre contable mensual | **Workflow 7:** Provisión contable individual mensual para blindar el pago futuro de indemnizaciones. |
| **`RequestQuinquenioPaymentUseCase`** | REST Controller (API) | Empleado (vía ESS) | **Workflow 7 / P8:** Inicia el cobro del quinquenio activando el cronómetro legal de 30 días calendario. |
| **`EvaluateQuinquenioPenaltyUseCase`**| Scheduled (Batch) | Cron diario (Motor de Mora Legal) | **Workflow 7 / P8:** Tarea diaria que barre los quinquenios solicitados; si detecta >30 días sin pago, inyecta la multa legal del 30%. |
| **`MarkQuinquenioPaidUseCase`** | Event-Driven / REST API | Sistema de Tesorería (ERP) o analista de RRHH (HR Admin) | **Workflow 7:** Confirma la liquidación de la deuda y resetea los contadores, preservando la antigüedad base. |
| **`RegisterHolidayUseCase`** | REST Controller (API) | Administrador de RRHH / HR Super User | **P10:** Feriados Regionales. Interfaz de configuración del calendario cívico de Santa Cruz (ej. 24 de Septiembre). |