# Resumen de la Iteración: Bounded Context 2 (Legal & Compliance)
## Integraciones, Ciclo de Vida del Contrato y Seguridad

Este documento resume la consolidación técnica del BC2, abarcando desde la integración con otros módulos del Core hasta la exposición de APIs seguras y documentadas.

---

## 🚀 1. Integraciones de Infraestructura (Outbound Adapters)

Se han implementado los adaptadores necesarios para que el BC2 se comunique con el resto del sistema sin violar las fronteras de su dominio.

### A. Auditoría Desacoplada (`AuditLogAdapter`)
* **Mecanismo:** Uso de `ApplicationEventPublisher` para emitir `AuditEvent`.
* **Seguridad:** El adaptador delega la captura de `tenantId` y `userId` al contexto de seguridad de Spring, evitando el paso manual de credenciales.
* **Trazabilidad:** Se utiliza un formato de cadena estructurado en el campo `action` para persistir datos críticos (Hashes, valores anteriores vs. nuevos).

### B. Evidencia Inmutable (`DigitalKardexAdapter`)
* **Responsabilidad:** Se delegó el cálculo criptográfico (SHA-256) al BC3 (Dossier/Kardex).
* **Flujo WORM:** El adaptador recibe el contenido crudo (`byte[]`), lo envía al BC3 mediante el comando `VerifyDocumentComplianceCommand` y retorna el hash generado para el registro legal del BC2.

---

## 🛠️ 2. Exposición de la API (Inbound REST Adapters)

Se han creado dos controladores especializados para organizar las capacidades del módulo, ambos documentados con **Swagger / OpenAPI 3** y manejando respuestas de error estándar (400, 401, 403, 404, 409, 500).

### A. `LegalComplianceController` (Configuración y Evidencia)
Gestiona los parámetros legales globales y la generación de pruebas de cumplimiento.
* **Endpoints:**
    * `PUT /thresholds/{ruleId}`: Actualización de Salario Mínimo y umbrales.
    * `POST /contracts/{contractId}/evidence`: Generación manual de evidencia WORM.
    * `POST /contracts/scan-expiring`: Disparador manual para el escaneo de Tácita Reconducción.

### B. `ContractLifecycleController` (Ciclo de Vida)
Gestiona el flujo transaccional del empleado desde su contratación hasta su salida.
* **Casos de Uso Implementados:**
    * `DraftContractUseCase`: Creación de borrador y reserva de posición.
    * `ApproveContractUseCase`: Activación del contrato (con validación SoD).
    * `ProposeContractAddendumUseCase`: Propuesta de cambios (Sueldo, Cargo).
    * `ApproveContractAddendumUseCase`: Aplicación de adendas con Effective Dating.
    * `TerminateContractUseCase`: Desvinculación y cierre de ciclo laboral.

---

## 🛡️ 3. Seguridad, Validación y Calidad de Código

### Prevención de Ataques XSS y Datos Sucios
* **Validación de Entrada:** Se implementó `jakarta.validation` en todos los DTOs (`Records`) de entrada.
* **Blindaje con Regex:** Se añadieron anotaciones `@Pattern` para asegurar que los campos de texto (`String`) como `projectId` y `tenantId` solo acepten caracteres alfanuméricos seguros, bloqueando inyecciones de script.
* **Gestión de Falsos Positivos:** Ante advertencias de seguridad en tipos no vulnerables (como `LocalDate` o `UUID`), se aplicó un análisis de trazabilidad (Taint Analysis) confirmando que el tipado fuerte de Java y la validación previa neutralizan cualquier riesgo real.

---

## 📌 4. Siguientes Pasos (Pendientes de Implementación)

Para cerrar el módulo al 100%, queda pendiente automatizar la ejecución de los casos de uso mediante adaptadores no humanos:

### A. Implementar el Scheduler (`ContractScannerScheduler`)
* **Ubicación:** `infrastructure/inbound/scheduler/`.
* **Objetivo:** Ejecutar `ScanExpiringContractsUseCase` de forma diaria (Cron: `0 0 0 * * ?`) para procesar la Tácita Reconducción automáticamente.

### B. Implementar Event Listeners (`ContractIntegrationListeners`)
* **Ubicación:** `infrastructure/inbound/listener/`.
* **Objetivo:** Reaccionar a eventos de otros módulos (ej. `CandidateHiredEvent` de Reclutamiento) para disparar automáticamente el `DraftContractUseCase` o la generación de evidencia.