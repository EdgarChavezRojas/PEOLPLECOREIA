# Clasificación de Casos de Uso - Dossier & Talent (BC3)

Basado en la arquitectura del sistema **PeopleCoreIA**, los *Workflows* de negocio y las directrices Hexagonales (DDD), aquí tienes la categorización de cada Caso de Uso de la carpeta `application/usecase` según el Adaptador Primario que lo consume.

---

## 1. Tareas Programadas (Batch / Cron Jobs)
Estos casos de uso son consumidos por hilos en segundo plano (ej. `@Scheduled` en Spring) sin intervención humana ni contexto web, ejecutándose periódicamente según una expresión CRON.

* **`EvaluateDocumentExpirationsUseCase`**
    * **Justificación:** Implementa la política P7 y el Workflow 5 (Monitoreo de Vigencia). Es un proceso automatizado que escanea diariamente la base de datos buscando documentos críticos (ej. Carnet Sanitario) que estén a 30 días de expirar o que ya hayan expirado, para disparar eventos de advertencia (`HEALTH_CARD_EXPIRATION_WARNING`) o suspender la elegibilidad del empleado.

---

## 2. Consumidores de Eventos (Event-Driven)
Estos casos de uso actúan como *Event Listeners* o *Subscribers* (ej. RabbitMQ/Kafka). Reaccionan asíncronamente a eventos de dominio (Domain Events) emitidos por otros Bounded Contexts.

* **`CheckOffboardingAssetsUseCase`**
    * **Reacciona a:** `OFFBOARDING_INITIATED` o `RELATIONSHIP_ENDED` (Eventos provenientes del BC1: Workforce o BC2: Legal).
    * **Justificación:** Orquesta el Workflow 9 y la invariante de "Certificación de Devolución". Cuando RRHH inicia la baja de un empleado, el BC3 escucha ese evento y ejecuta este caso de uso para verificar si el trabajador tiene activos (laptops, llaves) en su poder. Si es así, bloquea el proceso de liquidación (Finiquito) emitiendo el evento de rechazo/bloqueo (`OFFBOARDING_BLOCKED_BY_ASSETS`).

---

## 3. Controladores REST (APIs / Interacción de Usuario)
Estos casos de uso son consumidos por el Frontend (React/Angular/Móvil) a través de peticiones HTTP. Responden a las acciones del empleado en el *Employee Self-Service (ESS)*, del supervisor en el *Manager Self-Service (MSS)*, o de RRHH en el *Backoffice*.

* **`AcknowledgeMemorandumUseCase`**
    * **Consumido por:** Controlador ESS.
    * **Justificación:** Workflow 12. Permite al trabajador aceptar y firmar digitalmente, o en su defecto rechazar, un memorándum de llamada de atención desde su portal de autoservicio.
* **`RecordDisciplinaryActionUseCase`**
    * **Consumido por:** Controlador MSS / RRHH.
    * **Justificación:** Workflow 12. Permite a los líderes o equipo de talento registrar faltas, subir la evidencia y emitir memorandos inalterables (WORM con SHA-256) al empleado.
* **`AddPerformanceSnapshotUseCase`**
    * **Consumido por:** Controlador MSS / RRHH.
    * **Justificación:** Dominio 15 (Talent Inventory). Permite registrar las calificaciones del desempeño periódico de un colaborador, alimentando su perfil de talento.
* **`UpdateSkillSetUseCase`**
    * **Consumido por:** Controlador ESS / MSS.
    * **Justificación:** Dominio 15. Permite al empleado declarar nuevas habilidades adquiridas o al supervisor actualizar el nivel de *proficiency* (básico, experto) de su equipo.
* **`AssignAssetUseCase`**
    * **Consumido por:** Controlador Backoffice (Gestor de Activos).
    * **Justificación:** Workflow 8. Utilizado por el personal de almacén o TI para vincular un activo de la empresa (ej. Laptop) a un trabajador, requiriendo un acta digital de entrega.
* **`ReturnAssetUseCase`**
    * **Consumido por:** Controlador Backoffice.
    * **Justificación:** Workflow 8. Se ejecuta cuando el trabajador devuelve físicamente el equipo. El gestor de almacén valida el estado y cierra el ciclo de custodia.
* **`ReportAssetInspectionUseCase`**
    * **Consumido por:** Controlador Backoffice / MSS.
    * **Justificación:** Workflow 8. Utilizado para auditorías cíclicas donde se reporta el estado físico actual o daños de un activo que sigue bajo custodia (no implica devolución).
* **`GenerateDigitalCertificateUseCase`**
    * **Consumido por:** Controlador ESS.
    * **Justificación:** Workflow 14. Acción directa del empleado desde su portal para descargar certificados de trabajo o de haberes automatizados con firma digital y validación QR (para bancos/cajas de salud).
* **`UpdateAcademicRankUseCase`**
    * **Consumido por:** Controlador MSS / Decanato (Backoffice).
    * **Justificación:** Workflow 10 (Educación). Usado por la comisión académica para aprobar una actualización en el historial de formación (TrainingHistory) que luego el BC1 escuchará para ascender al docente en el escalafón salarial.
* **`VerifyDocumentComplianceUseCase`**
    * **Consumido por:** Controlador ESS / Backoffice (RRHH).
    * **Justificación:** Workflow 5. Orquesta todo el Digital Kardex. Empleados lo usan para cargar un documento (Draft/Pending), y RRHH lo usa para aprobar (`APPROVE`), rechazar (`REJECT`) o expirar documentos críticos (carnet sanitario, títulos).
