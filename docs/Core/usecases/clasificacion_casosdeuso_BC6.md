# Análisis de Casos de Uso - Bounded Context 6 (Experience)
**Sistema:** PeopleCoreIA (Core RRHH Bolivia 2026)

De acuerdo con la arquitectura definida en `AGENTS.md`, la lógica de negocio vive en `core-platform` (donde están estos Use Cases), y la exposición recae en el `iam-service` u otros servicios de orquestación a través de Controladores (`*Controller`) y Orquestadores (`*Orchestrator`).

---

## 1. EmployeeSelfServiceUseCase (Autoservicio del Empleado)

Gestiona todas las interacciones directas del empleado con la plataforma (ESS).

| Método | Tipo de Disparador | Ruta/Evento Sugerido |
| :--- | :--- | :--- |
| `requestDataUpdate` | **REST Controller** | `POST /api/v1/ess/actions/data-update` |
| `requestCertificate` | **REST Controller** | `POST /api/v1/ess/actions/certificate` |
| `cancelDataUpdate` | **REST Controller** | `POST /api/v1/ess/actions/{actionId}/cancel` |
| `acknowledgeNotification` | **REST Controller** | `POST /api/v1/ess/notifications/{notifId}/acknowledge` |
| `requestLeave` | **REST Controller** | `POST /api/v1/ess/actions/leave` |

* **¿Por qué?** Todas estas acciones son interacciones síncronas iniciadas por un usuario humano desde su aplicación móvil o portal web. El empleado espera una confirmación inmediata (UUID de la acción generada o error si falla la validación).
* **¿Cómo se implementa?** En el `iam-service`, se crea un `EmployeeSelfServiceController` que recibe un DTO (ej. `LeaveRequestDto`). El controlador llama a un `EmployeeSelfServiceOrchestrator`, el cual invoca los métodos de este Use Case en el `core-platform`.

---

## 2. ManagerSelfServiceUseCase (Autoservicio de Jefaturas)

Gestiona la revisión y resolución de flujos por parte de niveles superiores (MSS), garantizando la Segregación de Funciones (SoD).

| Método | Tipo de Disparador | Ruta/Evento Sugerido |
| :--- | :--- | :--- |
| `approveDataChange` | **REST Controller** | `POST /api/v1/mss/actions/{actionId}/approve` |
| `rejectDataChange` | **REST Controller** | `POST /api/v1/mss/actions/{actionId}/reject` |

* **¿Por qué?** Al igual que el ESS, son aprobaciones manuales realizadas por gerentes o supervisores al revisar su bandeja de tareas.
* **¿Cómo se implementa?** Se expone mediante un `ManagerSelfServiceController`. El `ApprovedBy` se extrae directamente del token JWT (Contexto de Seguridad) en el `iam-service` para garantizar la inmutabilidad de la auditoría y evitar que el Front-End falsifique quién aprueba.

---

## 3. CrossBcEventConsumerUseCase (Consumidor de Eventos Integración)

Responsable de reaccionar a la actividad de otros Bounded Contexts (Accruals, Compliance, Dossier) de forma asíncrona.

| Método | Tipo de Disparador | Evento Escuchado |
| :--- | :--- | :--- |
| `handleQuinquenioPaymentOverdue`| **Event Listener** | `QUINQUENIO_PAYMENT_OVERDUE` (Desde BC 4/5) |
| `handleDocumentValidationRejected`| **Event Listener** | `DOCUMENT_VALIDATION_REJECTED` (Desde BC 3) |
| `handleEligibilitySuspended` | **Event Listener** | `ELIGIBILITY_SUSPENDED_BY_COMPLIANCE` (Desde BC 2/3) |

* **¿Por qué?** Evita el acoplamiento fuerte. El BC 6 no tiene por qué estar consultando constantemente si un documento fue rechazado; simplemente reacciona cuando el módulo de Dossier anuncia el rechazo.
* **¿Cómo se implementa?** Usando el patrón Outbox ya implementado y un Message Broker (RabbitMQ/Kafka) o Spring `@EventListener` (si se usa Spring Modulith local). Se requiere una clase `CrossBcEventListener` en la capa de infraestructura del BC 6 (la cual ya existe en el reporte) que consuma estos mensajes y delegue la ejecución a este Use Case.

---

## 4. AiPredictiveUseCase (Motor Predictivo e IA)

Orquesta el análisis de datos para generar alertas tempranas sobre pasivos laborales y rotación.

| Método | Tipo de Disparador | Ruta/Evento Sugerido |
| :--- | :--- | :--- |
| `registerPredictionModel` | **REST Controller / Script**| `POST /api/v1/ai/models` (Solo Super Admin) |
| `generateTacitaReconduccionAlert`| **Job / Batch** | `@Scheduled(cron = "0 0 2 * * *")` |
| `generateDisciplinaryAlert` | **Event Listener** | Al recibir `DISCIPLINARY_ACTION_REPORTED` |
| `generateQuinquenioLiabilityAlert`| **Job / Batch** | `@Scheduled(cron = "0 30 2 * * *")` |
| `dismissAlert` | **REST Controller** | `POST /api/v1/ai/models/{modelId}/alerts/{alertId}/dismiss` |
| `getActiveAlerts` | **REST Controller** | `GET /api/v1/ai/models/{modelId}/alerts` |

* **¿Por qué Job/Batch?** La "Tácita Reconducción" (T-90 días) y los "Quinquenios" son hitos cronológicos. No dependen de la acción de un usuario, sino del paso del tiempo. Requieren un proceso Batch nocturno que barra los contratos (`Contract`) y evalúe las fechas.
* **¿Por qué Event Listener?** La alerta disciplinaria requiere evaluar el historial *inmediatamente* después de que un empleado recibe un memorándum (ej. 3 memos en 6 meses).
* **¿Cómo se implementa?** 1. Las alertas asíncronas se disparan mediante un motor Quartz o `@Scheduled` en la capa `infrastructure` del módulo `ai-service`, que invoca el Use Case inyectando la fecha actual.
    2. La administración de modelos y visualización de alertas activas se expone por un controlador REST en el dashboard de RRHH.

---

## 5. SendNotificationUseCase (Mensajería)

Utilidad transversal para enviar notificaciones a los empleados.

| Método | Tipo de Disparador | Ruta/Evento Sugerido |
| :--- | :--- | :--- |
| `send` | **Internal / Event Listener** | Interceptores de otros casos de uso o eventos. |
| `markAsRead` | **REST Controller** | `POST /api/v1/notifications/{notifId}/read` |

* **¿Por qué?** El método `send` raramente será un endpoint expuesto directamente al Front-End (por seguridad); suele ser llamado internamente por otros orquestadores (ej. al terminar el Onboarding) o escuchando eventos genéricos de dominio. El método `markAsRead` sí debe ser un endpoint REST invocado cuando el usuario abre la notificación en la App Móvil.
* **¿Cómo se implementa?** `markAsRead` va a un `NotificationController`. Para el envío real (email/push), en la capa de infraestructura, un componente debe escuchar el evento `NotificationSentEvent` emitido por este Use Case y conectarse a Firebase Cloud Messaging (FCM) o un SMTP.