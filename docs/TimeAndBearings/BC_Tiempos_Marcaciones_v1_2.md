# PeopleCoreIA · BC Tiempos y Marcaciones · v1.2.0

**Módulo Oficial: Tiempos y Marcaciones**
Documentación Técnica de Bounded Context

| **Versión** | 1.2.0 – Extension-Based GeoZone & Coherencia Multi-BC |
| --- | --- |
| **Stack Objetivo** | Java 21 · Spring Boot · Hexagonal DDD |
| **Bounded Context** | BC-TM · Aggregates 14–16 |
| **Normativa** | Ley General del Trabajo Bolivia · D.S. 21060 |

> **Registro de Cambios v1.1 → v1.2**
> - **[GEO-01]** Reemplazo del modelo `geo_coords (POINT/TEXT)` + `zone_code (VARCHAR)` en la ACL de OrgUnit por el `Extension` ENUM departamental (SCZ, LP, CB, OR, PT, TJ, BE, PA). Coherencia con `OrgUnit.extension` de BC-01 Core (Aggregate 3).
> - **[GEO-02]** Simplificación de `GeoZoneReference` (ACL): eliminados `reference_latitude`, `reference_longitude`, `default_radius_meters`. Reemplazados por `org_extension` (ENUM).
> - **[GEO-03]** Actualización de `GeoValidationSnapshot`: `zone_code_snapshot` → `org_extension_snapshot` (ENUM).
> - **[GEO-04]** Actualización de Policy P-TM28 con lógica de validación basada en departamentos.
> - **[GEO-05]** Renombrado del evento upstream `ORG_UNIT_GEO_ZONE_UPDATED` → `ORG_UNIT_EXTENSION_UPDATED`.
> - **[COHERENCIA]** Sin cambios directos en BC-TM derivados del `Resumen de Cambios` (aplican a BC-01/02/04/05). BC-TM es coherente con `PersonIdentity`, `EmploymentAgreement`, `AccrualVault` e `IndemnizableTrimSnapshot` a través de sus referencias por `relationship_id` opaco.

---

## Notas Arquitectónicas y Guía de Lectura

Esta sección aclara las decisiones de diseño y las convenciones del documento para que todo miembro del equipo comprenda el rol exacto de este módulo dentro del sistema PeopleCoreIA.

### ¿Qué es Upstream y Downstream en este documento?

Estos términos describen la dirección del flujo de datos entre Bounded Contexts, no quién "llama" a quién de forma sincrónica:

- **Upstream (BC-TM consume de…):** Son los BCs de los que BC-TM necesita datos para operar. BC-TM los consulta mediante queries síncronas a través de una Anti-Corruption Layer (ACL). Nunca escribe en sus tablas.

- **Downstream (BC-TM produce para…):** Son los BCs que dependen de los datos que BC-TM genera. BC-TM NO los llama directamente ni les envía datos en tiempo real. Lo que hace es publicar un evento en un Message Broker (ej. Kafka). Los módulos downstream escuchan ese tópico y consumen el evento cuando están listos. BC-TM no sabe ni le importa quién lo escucha. El desacoplamiento es total.

> **📌 Analogía de Downstream**
> BC-TM es una emisora de radio que transmite eventos.
> BC-05 (Payroll) es quien tiene el receptor sintonizado a ese canal.
> Si BC-05 está apagado, el evento espera en el broker. BC-TM ya terminó su trabajo.
> BC-TM nunca bloquea su operación esperando que BC-05 responda.

### ¿BC-05 es un Bounded Context dentro de T&M?

No. BC-05 (Financial & Payroll) es el módulo de Planillas del sistema PeopleCoreIA. Está al mismo nivel jerárquico que BC-TM, BC-SCH (Scheduling), y BC-01 (Core HR). Son cuatro módulos independientes del mismo sistema, no uno dentro del otro.

> **📌 Mapa de Módulos del Sistema**
> BC-01  → Core HR (organización, personas, contratos, calendario)
> BC-SCH → Scheduling (planificación de turnos, mallas, rostering)
> BC-TM  → Tiempos y Marcaciones (este módulo — marcación, excepciones, cierre)
> BC-04  → Accruals (vacaciones, antigüedad, saldos)
> BC-05  → Financial & Payroll (nómina, liquidación, finiquito)
> Todos son módulos independientes. Ninguno vive dentro de otro.

### ¿Cómo se conecta BC-TM a Core (BC-01)?

BC-TM consulta a Core como fuente de verdad para las reglas que no le pertenecen. La regla de diseño es: BC-TM solo sabe hacer una cosa — capturar la realidad física del tiempo trabajado. Todo lo demás lo consulta.

**Reglas que BC-TM consulta a Core en tiempo real:**

- **¿El colaborador está activo?** → `Relationship.status` en BC-01. Sin esto, no puede marcar.
- **¿En qué departamento está la unidad?** → `OrgUnit.extension` (ENUM: SCZ, LP, CB, OR, PT, TJ, BE, PA) en BC-01. Es el código departamental de Bolivia que Core gestiona. BC-TM lo consume para la validación geográfica de marcaciones móviles. **[GEO-01 NUEVO]**
- **¿Qué días son feriados?** → `CalendarHoliday` en BC-01. Para clasificar horas con recargo.
- **¿Dónde vive el colaborador (teletrabajo)?** → `Person.address_record` en BC-01. Para geocerca extendida.

**Eventos de Core a los que BC-TM se suscribe:**

- `EMPLOYEE_DEACTIVATED` → BC-TM revoca automáticamente todos los `BiometricEnrollment` activos del colaborador.
- `ORG_UNIT_EXTENSION_UPDATED` → BC-TM invalida el caché del `org_extension` y fuerza re-consulta. **[GEO-05 RENOMBRADO]**
- `HOLIDAY_CALENDAR_PUBLISHED` → BC-TM actualiza la referencia de feriados para el CRON de consolidación.

### Delimitación de Responsabilidades con BC-SCH (Scheduling)

| **Responsabilidad** | **Dueño correcto** | **Nota** |
| --- | --- | --- |
| Creación y publicación de la malla de turnos (Rostering) | BC-SCH | Scheduling es el único dueño del AssignedShift. |
| Intercambio y asignación de turnos (Swap, Open Shifts) | BC-SCH | |
| Motor de constraints de planificación (horarios legales) | BC-SCH | |
| Captura de la marcación real del colaborador | BC-TM | Este módulo. |
| Cálculo de desviaciones teórico/real | BC-TM | BC-TM consulta el AssignedShift de BC-SCH como referencia de solo lectura. |
| Gestión de excepciones de asistencia | BC-TM | |
| Consolidación y cierre de timesheet para nómina | BC-TM | El WF3 y WF4 de Scheduling son redundantes con BC-TM y deben ser removidos de ese módulo. |

### Diseño No-Bloqueante (Non-Blocking Design)

Principio fundamental: ninguna falla individual debe detener la cola de marcaciones de otros colaboradores. En entornos de kiosco con fila de empleados, un error de un colaborador específico (biometría fallida, geocerca incorrecta, fraude detectado) genera un registro de excepción asíncrono pero el dispositivo permanece operativo al 100% para el siguiente en la fila.

> **📌 Regla de Oro del Diseño No-Bloqueante**
> ✓ Los errores de marcación generan excepciones asíncronas (TimeDeviationRecord / PunchAttemptLog).
> ✓ El dispositivo (kiosco/lector) continúa operativo tras cualquier falla individual.
> ✓ El MSS resuelve las excepciones desde su panel, desacoplado del momento de la marcación.
> ✗ Está prohibido bloquear el dispositivo ni la cola por la falla de un colaborador.
> ✗ La única excepción son desastres de infraestructura (servidor caído, red cortada), no errores de negocio.

---

## Extension ENUM: Código Departamental Bolivia

**[GEO-01 NUEVO]** BC-TM consume el campo `OrgUnit.extension` de BC-01 Core. Este ENUM identifica el departamento boliviano de cada unidad organizacional. Es la base del modelo de geo-validación simplificado.

```java
public enum Extension {
    SCZ("Santa Cruz"),
    LP("La Paz"),
    CB("Cochabamba"),
    OR("Oruro"),
    PT("Potosí"),
    TJ("Tarija"),
    BE("Beni"),
    PA("Pando");

    private final String name;
    Extension(String name) { this.name = name; }
}
```

**Justificación del modelo de geo-zona por departamento:** En el contexto operativo boliviano, la gran mayoría de las empresas operan dentro de un único departamento. La validación departamental es suficiente para determinar la coherencia geográfica de una marcación móvil, es legible por el equipo de RRHH sin conocimientos técnicos de coordenadas, y es consistente con la infraestructura de datos ya existente en `OrgUnit.extension` de BC-01 Core. La geolocalización GPS del dispositivo móvil se sigue capturando en `GeoValidationSnapshot` para trazabilidad y auditoría; la validación de cumplimiento se delega al departamento.

---

## Workflows Operativos del Módulo de Tiempos y Marcaciones

### 1. Workflow de Marcación en Tiempo Real (Real-Time Clocking)

Es el proceso central y más crítico del módulo. Ocurre cada vez que un colaborador registra su presencia física o remota. La atomicidad, la seguridad y la precisión temporal son no negociables.

| **Actor Principal** | Trabajador (ESS / Kiosco / Dispositivo Biométrico) |
| --- | --- |
| **Propósito** | Registrar un evento de marcación (entrada, salida, inicio/fin de descanso) con validación de identidad, ubicación y tiempo de servidor, generando un TimeEntry inmutable vinculado al AttendanceLedger del día. |

**Flujo Paso a Paso:**

1. **Intento de Marcación:** El colaborador accede al canal disponible: App ESS (móvil), Kiosco físico, o Dispositivo Biométrico de huella/facial instalado en el local.

2. **Captura de Contexto Inmutable:** La capa de aplicación captura tres datos en un solo snapshot atómico: (1) La hora oficial del servidor NTP — nunca la hora del dispositivo cliente. (2) Las coordenadas GPS del dispositivo (si es canal móvil). (3) El identificador del dispositivo (`device_id`).

3. **Validación de Identidad (Anti-Fraude):** Según la política configurada por el Tenant (P-TM30), el sistema exige: PIN + biometría facial (canal móvil) o huella dactilar registrada (kiosco/biométrico). Una falla en la validación biométrica genera un `PunchAttemptLog` con estado `BIOMETRIC_FAIL` y notifica al supervisor. El dispositivo NO se bloquea.

4. **Validación de Geo-Extensión (Extension-Based Geo-Fencing):** **[GEO-01 ACTUALIZADO]** Para marcaciones desde canal móvil, el motor consulta el `org_extension` (ENUM departamental) de la `OrgUnit` en BC-01 Core. Si las coordenadas GPS del colaborador no corresponden al departamento configurado en `org_extension` (según P-TM28), el sistema crea el `TimeEntry` con `geo_status = OUTSIDE_FENCE` y genera una excepción `GEO_VIOLATION` de forma asíncrona. La marcación se completa sin bloquear al colaborador ni al dispositivo. El MSS resuelve la excepción desde su panel.

5. **Determinación del Tipo de Evento (Punch Type Resolution):** El motor evalúa el estado actual del `AttendanceLedger` para el día y determina automáticamente el `punch_type`: si no existe ningún IN activo → `PUNCH_IN`; si existe un IN sin OUT → `PUNCH_OUT`; si existe par IN/OUT y el turno tiene pausa → `MEAL_START` / `MEAL_END`.

6. **Creación del TimeEntry:** Se persiste el evento de marcación como un registro inmutable (append-only). No se puede editar un TimeEntry ya creado; cualquier corrección genera un nuevo TimeEntry con `punch_type = MANUAL_CORRECTION` y referencia al entry original.

7. **Cruce Teórico/Real (Real-Time Deviation Check):** El motor compara el `punch_time` contra el `expected_start` o `expected_end` del `AssignedShift` del módulo Scheduling (BC-SCH). Si la desviación supera los umbrales de P-TM26 y P-TM27, se crea un `TimeDeviationRecord` con estado `PENDING` de forma asíncrona.

8. **Notificación Inmediata:** Si se detecta `LATE_IN` o `GEO_VIOLATION`, se publica el evento asíncrono `PUNCH_ANOMALY_DETECTED` que la capa de notificaciones de BC-01 Core convierte en push notification al MSS. El flujo de marcación ya terminó antes de que esto ocurra.

### 2. Workflow de Gestión de Excepciones y Justificaciones (Exception Handling)

Cubre el ciclo de vida completo de cualquier desviación, desde su creación automática hasta su resolución, determinando si se paga, justifica o descarta.

| **Actor Principal** | Manager (MSS) / Analista de Planillas / Trabajador (ESS – justificaciones propias) |
| --- | --- |
| **Propósito** | Proveer un canal estructurado para revisar, justificar, aprobar o rechazar cada TimeDeviationRecord, garantizando que el AttendanceLedger quede matemáticamente cerrado y listo para el motor de nómina. |

**Flujo Paso a Paso:**

1. **Generación Automática de Excepción:** El CRON de consolidación nocturna (WF-TM03) o el cruce en tiempo real (WF-TM01) crean `TimeDeviationRecord`. Los tipos posibles son: `LATE_IN`, `EARLY_OUT`, `OVERTIME`, `NO_SHOW`, `GEO_VIOLATION`, `MISSING_PUNCH` y `UNAUTHORIZED_ABSENCE`.

2. **Priorización en la Bandeja:** El MSS accede al panel de gestión de excepciones, ordenado por: (1) Gravedad (`NO_SHOW > OVERTIME > GEO_VIOLATION > LATE_IN`). (2) Antigüedad (excepciones cercanas al límite de la ventana P-TM31).

3. **Flujo de Resolución – Rama AUSENCIA/NO_SHOW:** El MSS puede: (A) Vincular a una `LeaveTransaction` existente en BC-04 → `JUSTIFIED_LEAVE`. (B) Cargar evidencia → `JUSTIFIED_DOCUMENT`. (C) Marcar como `UNJUSTIFIED_ABSENCE` → impacta descuento en nómina.

4. **Flujo de Resolución – Rama OVERTIME:** El MSS puede: (A) Aprobar (`APPROVED`) → suma al cálculo en BC-05. (B) Rechazar (`REJECTED`) → ajusta la salida efectiva al `expected_end` teórico. Toda aprobación requiere `reason_note` obligatoria.

5. **Flujo de Resolución – Rama MISSING_PUNCH / GEO_VIOLATION:** El MSS ingresa la hora correcta manualmente. El sistema crea un `TimeEntry` con `source = MANUAL`. El `TimeDeviationRecord` queda en estado `OVERRIDDEN_BY_MANAGER`.

6. **Auto-Escalado por Vencimiento:** Si una excepción no se resuelve antes del fin de la ventana P-TM31 (72 horas laborales), el sistema escala al nivel jerárquico superior y marca la desviación como `AUTO_CLOSED_AS_UNJUSTIFIED`.

7. **Auditoría:** Cada acción genera una entrada inmutable en el `ExceptionAuditLog` (quién, cuándo, qué decisión, qué nota).

### 3. Workflow de Consolidación y Cierre de Periodo (Timesheet Consolidation)

Convierte la realidad bruta de marcaciones en datos procesables para la nómina. Es un proceso mixto: automático (CRON) con puntos de control humano (MSS).

| **Actor Principal** | Sistema (CRON automático) + Manager (MSS) o Analista de Planillas |
| --- | --- |
| **Propósito** | Consolidar todos los AttendanceLedger del periodo, resolver excepciones pendientes, calcular el WorkedHoursSummary validado y emitir el evento ATTENDANCE_PERIOD_CLOSED hacia BC-05 (Payroll) a través del Message Broker. |

**Flujo Paso a Paso:**

1. **Disparo del CRON Nocturno (00:30 AM, hora del servidor):** El job de consolidación diaria ejecuta un barrido sobre todos los `AttendanceLedger` con `status = OPEN` o `PENDING_REVIEW` cuya `work_date` corresponde al día anterior.

2. **Cruce Teórico/Real (Batch):** Para cada `AttendanceLedger`, el motor compara los `TimeEntry` registrados contra el `AssignedShift` de BC-SCH. En este paso se detectan y crean en batch los `TimeDeviationRecord` no generados en tiempo real (ej. `NO_SHOW` completo).

3. **Cálculo de Horas Efectivas:** Horas Brutas = `PUNCH_OUT.time − PUNCH_IN.time`. Se aplican deducciones de pausas según P-TM29. El resultado se almacena en `WorkedHoursSummary` (`regular_hours`, `overtime_hours`, `night_hours`, `holiday_hours`).

4. **Clasificación de Horas Especiales:**
   - Horas Nocturnas (22:00–06:00, recargo 25% LGT Bolivia).
   - Horas Feriado (cruce con `CalendarHoliday` de BC-01, recargo 100%).
   - Horas Domingo (recargo 100% según ley boliviana).

5. **Transición de Estado del Ledger:** Sin excepciones pendientes → `CLOSED`, `is_finalized = TRUE`. Con excepciones pendientes → `PENDING_REVIEW`, notifica al MSS.

6. **Cierre de Periodo:** El MSS o Analista ejecuta el cierre. El sistema verifica que todos los `AttendanceLedger` del rango estén `CLOSED`. Si existen `PENDING_REVIEW`, el sistema muestra el listado de bloqueos.

7. **Emisión del Evento de Nómina:** Al cerrar el periodo, BC-TM publica el evento `ATTENDANCE_PERIOD_CLOSED` en el Message Broker con el payload `PayrollHandoffPackage`. BC-05 consume ese evento de forma asíncrona para iniciar el cálculo de nómina. BC-TM no espera respuesta de BC-05.

8. **Bloqueo de Retroactividad (Record Lock):** Una vez emitido el evento, todos los `AttendanceLedger` del periodo quedan inmutables (P-TM33). Correcciones posteriores requieren un proceso de Reliquidación.

### 4. Workflow de Gestión de Dispositivos y Biometría (Device Lifecycle Management)

Cubre el ciclo de vida de los dispositivos físicos de marcación y la gestión del template biométrico de cada colaborador.

| **Actor Principal** | Administrador de TI / Analista de RRHH / Trabajador (solo para enrolamiento propio) |
| --- | --- |
| **Propósito** | Registrar, activar, auditar y dar de baja dispositivos de marcación; gestionar el enrolamiento y revocación de credenciales biométricas, garantizando trazabilidad anti-fraude sin interrumpir la operación. |

**Flujo – Alta de Dispositivo:**

1. **Registro de Dispositivo:** El Admin de TI registra el hardware en el `ClockingDevice Registry`: número de serie, tipo (`KIOSK`, `BIOMETRIC_READER`, `NFC_TERMINAL`, `MOBILE_APP`), `OrgUnit` asignada y capacidades (`FACIAL`, `FINGERPRINT`, `NFC`, `QR`).

2. **Generación de Certificado de Autenticación:** El sistema genera un par de claves asimétricas. La clave privada se instala en el hardware; la pública queda en `DeviceCapabilities`. Toda marcación desde ese dispositivo lleva firma digital verificable.

3. **Sincronización de Listas de Empleados:** El dispositivo se sincroniza con el padrón de colaboradores activos de la `OrgUnit`. Solo los `relationship_id` con estado `ACTIVE` en BC-01 reciben credenciales.

**Flujo – Enrolamiento Biométrico:**

1. **Solicitud de Enrolamiento:** El Analista de RRHH inicia el proceso para un colaborador activo. El sistema verifica que no tenga un template activo previo del mismo tipo (`FINGERPRINT/FACIAL`).

2. **Captura del Template:** Se capturan múltiples muestras biométricas (mínimo 3 para huella, 5 para facial). El template normalizado se almacena cifrado; nunca se guarda la imagen raw.

3. **Asociación y Activación:** El `BiometricEnrollment` queda vinculado al `relationship_id` y al `device_id`. Estado inicial: `ACTIVE`.

**Flujo – Baja/Revocación:**

1. **Trigger de Revocación:** (A) Desvinculación del colaborador → revocación automática vía evento `EMPLOYEE_DEACTIVATED` de BC-01. (B) Pérdida/robo de dispositivo → revocación de emergencia. (C) Solicitud del colaborador.

2. **Invalidación:** El `BiometricEnrollment` pasa a estado `REVOKED`. El dispositivo recibe señal de sincronización para eliminar el template local en el siguiente heartbeat. El dispositivo continúa operativo para todos los demás empleados.

3. **Auditoría de Intentos Post-Revocación:** Todo intento con un template revocado es registrado en `PunchAttemptLog` con `security_incident = TRUE` y notifica al equipo de seguridad. El dispositivo no se interrumpe.

### 5. Workflow de Autorización de Trabajo Remoto (Remote Work & Extended Geo-Validation)

Gestiona el proceso por el cual un colaborador puede marcar desde una ubicación distinta a su centro habitual.

| **Actor Principal** | Trabajador (ESS) + Manager (MSS) |
| --- | --- |
| **Propósito** | Permitir marcaciones fuera del departamento estándar para trabajo remoto o comisiones, manteniendo la trazabilidad de ubicación y previniendo el fraude de marcación proxy en entornos distribuidos. |

**Flujo Paso a Paso:**

1. **Solicitud de Jornada Remota:** El colaborador solicita con al menos 24 horas de antelación (configurable) desde la App ESS, indicando el motivo: `TELETRABAJO`, `COMISION_SERVICIO` o `CONTINGENCIA`.

2. **Aprobación del MSS:** El gerente aprueba o rechaza desde el panel MSS. La aprobación crea un `RemoteWorkAuth` con fechas y motivo autorizados, vinculado al `relationship_id`.

3. **Marcación en Modo Remoto:** Durante jornadas autorizadas, el sistema ignora la restricción del `org_extension` estándar de la `OrgUnit`. En su lugar activa la validación extendida según el tipo de remoto (P-TM28).

4. **Captura de Evidencia Remota:** Según la política del Tenant: fotografía del lugar (`CONTINGENCIA`) o coordenadas GPS periódicas / Location Breadcrumb (`TELETRABAJO`).

5. **Detección de Patrón Anómalo:** Si las coordenadas GPS registradas indican países diferentes entre `PUNCH_IN` y `PUNCH_OUT` de la misma jornada, se genera una `GEO_VIOLATION` crítica para revisión de seguridad. La marcación ya fue registrada; la excepción es asíncrona.

6. **Cierre del AttendanceLedger Remoto:** Sigue el flujo WF-TM03. El `WorkedHoursSummary` queda marcado con `remote_work = TRUE` para diferenciarlo en reportes de nómina y auditoría.

---

## Políticas del Módulo de Tiempos y Marcaciones

### Clúster de Captura y Validación de Marcaciones (Clocking Integrity)

---

**P-TM26: Política de Autoridad Temporal del Servidor (NTP Time Authority)**

| | |
| --- | --- |
| **Propósito** | Garantizar que ninguna marcación pueda ser manipulada ajustando el reloj del dispositivo cliente, preservando la integridad legal de todos los registros. |
| **Regla de Negocio** | El único tiempo válido para `punch_time` es el del servidor NTP, capturado en el momento exacto de la solicitud. La app cliente NUNCA transmite su propio timestamp; solo envía datos biométricos/GPS y espera que el servidor asigne el tiempo. Desfase NTP > 5 segundos genera alerta de infraestructura crítica. |
| **Impacto en Sistema** | El campo `punch_time` es asignado exclusivamente por la capa de aplicación del servidor. Si llega un timestamp embebido del cliente, se ignora y descarta. Se genera un `PunchAttemptLog` con flag `timestamp_tamper_detected = TRUE` para auditoría. |

---

**P-TM27: Política de Anti-Duplicación de Marcaciones (Anti-Double-Punch)**

| | |
| --- | --- |
| **Propósito** | Evitar que un colaborador genere dos marcaciones del mismo tipo por error de usuario o reenvíos duplicados por fallo de conectividad. |
| **Regla de Negocio** | El sistema aplica idempotencia en la capa de aplicación: si se recibe una solicitud del mismo `relationship_id` y `punch_type` dentro de una ventana de 5 minutos (configurable), la segunda solicitud se descarta sin crear un nuevo `TimeEntry`. Se devuelve el `TimeEntry` original al cliente. Para `PUNCH_IN` múltiple fuera de la ventana de 5 min, se genera una excepción `DUPLICATE_PUNCH` para revisión del MSS. |
| **Impacto en Sistema** | La solicitud duplicada no genera un nuevo `TimeEntry`. El sistema responde con el `entry_id` original (respuesta idempotente). Si la duplicación ocurre con más de 5 minutos de diferencia, se crea el `TimeEntry` con `punch_type = DUPLICATE_REVIEW` y se notifica al MSS. El dispositivo no se interrumpe en ningún caso. |

---

**P-TM28: Política de Zona Geográfica y Marcación Remota (Extension-Based Geo-Zone & Remote Work Validation)** **[GEO-04 ACTUALIZADO]**

| | |
| --- | --- |
| **Propósito** | Definir los umbrales de validación geográfica para marcaciones estándar y remotas usando el código departamental (`Extension` ENUM) configurado en BC-01 Core para cada OrgUnit, balanceando control presencial con flexibilidad del trabajo distribuido. |
| **Regla de Negocio** | La geocerca no es un polígono ni un punto de coordenadas almacenado en BC-TM. BC-TM consulta el `org_extension` (ENUM: SCZ, LP, CB, OR, PT, TJ, BE, PA) de la `OrgUnit` en BC-01 Core. Este código identifica el departamento boliviano donde opera la unidad. La validación por modo es: **ESTÁNDAR:** Las coordenadas GPS del dispositivo deben corresponder al departamento indicado por `org_extension`. Si el dispositivo reporta ubicación fuera del departamento, se genera `GEO_VIOLATION`. **COMISION_SERVICIO:** Sin restricción departamental; el `RemoteWorkAuth` aprobado debe estar activo y las coordenadas GPS se registran para trazabilidad. **TELETRABAJO:** Las coordenadas GPS del dispositivo deben coincidir con el departamento del `AddressRecord` del colaborador en BC-01 (puede ser distinto al departamento de la OrgUnit si el colaborador vive en otra ciudad). **CONTINGENCIA:** Sin restricción de zona; fotografía de evidencia obligatoria. Las coordenadas GPS se registran. |
| **Impacto en Sistema** | Si las coordenadas no corresponden al departamento `org_extension`: se crea el `TimeEntry` con `geo_status = OUTSIDE_FENCE`. El `AttendanceLedger` queda en `PENDING_REVIEW`. Se genera una excepción `GEO_VIOLATION` asignada al MSS de forma asíncrona. La marcación se completa y el dispositivo queda inmediatamente disponible para el siguiente colaborador en la fila. |

---

### Clúster de Control de Fraude y Autenticación (Anti-Fraud)

---

**P-TM29: Política de Autenticación Biométrica Progresiva (Biometric Auth Levels)**

| | |
| --- | --- |
| **Propósito** | Establecer niveles de autenticación proporcionales al riesgo del canal de marcación, sin comprometer la fluidez operacional del dispositivo. |
| **Regla de Negocio** | NIVEL 1 – Kiosco Físico: Huella dactilar únicamente (entorno controlado). NIVEL 2 – App Móvil en zona estándar (departamento correcto): PIN de 6 dígitos + Biometría facial del SO (FaceID/Android Biometric API). NIVEL 3 – App Móvil fuera de zona (con `RemoteWorkAuth`): NIVEL 2 + confirmación de coordenadas GPS explícita. NIVEL 4 – Marcación Manual/Web por MSS: 2FA del MSS, genera `TimeEntry` con `source = MANUAL`. |
| **Impacto en Sistema** | Si el nivel de auth no se satisface, la solicitud se rechaza con `AUTH_LEVEL_INSUFFICIENT` y se registra en `PunchAttemptLog`. Ante fallos biométricos consecutivos del mismo colaborador: se registra el intento con `auth_result = BIOMETRIC_FAIL` y se notifica al MSS. El dispositivo NO se bloquea y continúa operativo para los demás colaboradores en cola. |

---

**P-TM30: Política de Tolerancia Cero al Proxy Clocking (Buddy Punching Prevention)**

| | |
| --- | --- |
| **Propósito** | Eliminar la posibilidad de que un colaborador marque en nombre de otro, sin interrumpir la operación del dispositivo. |
| **Regla de Negocio** | El sistema requiere biometría facial en vivo (liveness detection) para canal móvil, detectando ataques de foto, video y máscaras. Para kioscos físicos: la huella es individual e intransferible. Si el motor detecta un intento de proxy clocking (patrón de huella que coincide con otro colaborador registrado), el intento es rechazado y se genera un `SecurityIncidentRecord`. |
| **Impacto en Sistema** | Si se detecta un intento de proxy clocking: el `TimeEntry` queda marcado con `fraud_flag = TRUE` en estado `REJECTED`. Se crea un `SecurityIncidentRecord` con nivel `CRITICAL`. Se notifica al MSS y al rol de Seguridad/RRHH. El collaborador cuya biometría fue usada fraudulentamente recibe alerta. El `AttendanceLedger` queda en `PENDING_REVIEW` hasta intervención del MSS. El dispositivo permanece completamente operativo para el resto de la fila. |

---

### Clúster de Gestión de Excepciones y Ausencias (Exception Management)

---

**P-TM31: Política de Ventana de Justificación (Justification Window)**

| | |
| --- | --- |
| **Propósito** | Establecer un plazo máximo para resolver excepciones, evitando que el cierre de nómina sea bloqueado indefinidamente. |
| **Regla de Negocio** | Todo `TimeDeviationRecord` debe ser resuelto dentro de 72 horas laborales desde su creación. Para `NO_SHOW` en día de cierre de periodo, la ventana se reduce a 24 horas. El sistema escala automáticamente al nivel jerárquico superior cada 24 horas de inactividad (Manager → Gerente Regional → RRHH Central). |
| **Impacto en Sistema** | Al cumplirse la ventana sin resolución: se ejecuta el cierre automático `AUTO_CLOSED_AS_UNJUSTIFIED`. Ausencias: descuento de día completo sin reversión posterior al cierre. Overtime: se descarta automáticamente. El auto-cierre queda registrado en `ExceptionAuditLog` con `actor = SYSTEM` y razón `WINDOW_EXPIRED`. |

---

**P-TM32: Política de Marcación Retroactiva (Retroactive Punch)**

| | |
| --- | --- |
| **Propósito** | Regular cómo un MSS puede registrar o corregir manualmente una marcación de un día pasado, evitando alteraciones encubiertas. |
| **Regla de Negocio** | Un MSS puede crear un `TimeEntry` retroactivo (`source = MANUAL`) solo si: (1) El `AttendanceLedger` está en `OPEN` o `PENDING_REVIEW`. (2) La fecha no pertenece a un periodo ya cerrado y transmitido a nómina. (3) El MSS registra `reason_note` con mínimo 20 caracteres. (4) Si la retroactividad supera 48 horas, requiere doble firma de un segundo nivel. Está prohibido modificar un `TimeEntry` existente; solo se puede agregar uno nuevo con referencia al entry corregido. |
| **Impacto en Sistema** | El `TimeEntry` retroactivo queda marcado con `is_retroactive = TRUE`, `approver_id` del MSS y `secondary_approver_id` si aplica. Se genera un `RetroactiveAuditRecord` inmutable. Si se intenta crear en un periodo ya `CLOSED`, se rechaza con `PERIOD_LOCKED` y se redirige al proceso de Reliquidación. |

---

### Clúster de Cierre y Transmisión a Nómina (Payroll Hand-Off)

---

**P-TM33: Política de Inmutabilidad del Registro Cerrado (Closed Record Immutability)**

| | |
| --- | --- |
| **Propósito** | Garantizar que un AttendanceLedger cerrado y transmitido a BC-05 (Payroll) no pueda ser alterado, preservando la integridad del proceso de liquidación. |
| **Regla de Negocio** | Un `AttendanceLedger` en estado `CLOSED` con `is_finalized = TRUE` es completamente inmutable. Ningún actor (ni el Administrador del Sistema) puede modificar directamente sus `TimeEntry`, `WorkedHoursSummary` o `TimeDeviationRecord`. Las correcciones posteriores al cierre se hacen vía Reliquidación: un nuevo conjunto de registros de ajuste que no toca los originales (principio de journal entry: suma o resta el delta). |
| **Impacto en Sistema** | Todo intento de modificación directa de un registro `CLOSED` es rechazado con `ClosedRecordMutationException` a nivel de dominio. Se genera una `SecurityAuditEntry` con el actor, timestamp y dato que se intentó modificar, escalando al rol de Auditoría Interna. |

---

**P-TM34: Política de Periodo de Gracia para Cierre de Periodo (Closure Grace Period)**

| | |
| --- | --- |
| **Propósito** | Proveer una ventana entre el fin del periodo de trabajo y el cierre oficial del timesheet para que los actores resuelvan las últimas excepciones sin bloquear la fecha de pago. |
| **Regla de Negocio** | El cierre de periodo puede ejecutarse hasta 3 días hábiles después del último día del periodo. Durante este Periodo de Gracia: (1) Los `AttendanceLedger` siguen aceptando modificaciones. (2) WF-TM02 sigue activo. (3) No se puede emitir `ATTENDANCE_PERIOD_CLOSED` aún. Al vencer los 3 días: el sistema aplica el auto-cierre masivo (P-TM31) y procede con el cierre automáticamente. |
| **Impacto en Sistema** | El sistema muestra un countdown en el panel de Gestión de Tiempo. Al día 3 a las 17:00 hora local del Tenant, el CRON ejecuta el cierre masivo de todos los registros pendientes y emite `ATTENDANCE_PERIOD_CLOSED` al Message Broker sin intervención humana adicional. |

---

## Invariantes del Módulo de Tiempos y Marcaciones

Las invariantes son contratos de consistencia del dominio que NUNCA pueden ser violados. Son protecciones escritas directamente en el Aggregate Root y se validan antes de cada persistencia.

### 1. Invariantes del Agregado: AttendanceLedger

**Invariante de Singularidad de Punch Activo (Active Punch Uniqueness)**

- **Regla:** En cualquier punto en el tiempo, un `relationship_id` puede tener como máximo UN `TimeEntry` de tipo `PUNCH_IN` sin `PUNCH_OUT` correspondiente para una `work_date` dada.
- **Por qué es vital:** Un colaborador no puede estar "adentro" dos veces al mismo tiempo. Viola el cálculo de `WorkedHoursSummary` y es la puerta de entrada al fraude de marcación doble.

**Invariante de Secuencia Cronológica y Causalidad (Chronological Integrity & No Time-Travel)**

- **Regla A:** Todo `PUNCH_OUT` o `MEAL_START` debe tener `punch_time` estrictamente POSTERIOR al `PUNCH_IN` correspondiente. Duración resultante debe ser > 0 segundos.
- **Regla B:** Ningún `TimeEntry` puede tener `punch_time` en el futuro relativo al timestamp del servidor NTP. Tolerancia de sincronización: ±5 segundos.
- **Regla C (Cruce de Medianoche):** Un `AttendanceLedger` puede contener `TimeEntry` del día siguiente (turno nocturno 22:00–06:00). El `work_date` del ledger es el día de inicio del turno. El `PUNCH_OUT` del día siguiente se adjudica al ledger del día anterior.
- **Por qué es vital:** Impide jornadas de duración negativa o marcaciones en el futuro que harían que el motor de nómina procese datos absurdos.

**Invariante de Paridad para el Cierre del Ledger (Attendance Closure Parity)**

- **Regla:** Un `AttendanceLedger` NO puede transicionar a `CLOSED` / `is_finalized = TRUE` si tiene un número impar de `TimeEntry` {`PUNCH_IN`, `PUNCH_OUT`} vigentes (entrada sin salida), a MENOS que exista un `TimeDeviationRecord` de tipo `MISSING_PUNCH` con estado `OVERRIDDEN_BY_MANAGER` que cubra ese hueco.
- **Por qué es vital:** La nómina opera sobre datos matemáticamente completos. Transmitir un ledger abierto a BC-05 causa errores de cálculo o pagos incorrectos.

**Invariante de Inmutabilidad Post-Cierre (Finalized Record Immutability)**

- **Regla:** Una vez `is_finalized = TRUE`, ningún campo de ninguna entidad hija (`TimeEntry`, `WorkedHoursSummary`, `TimeDeviationRecord`) puede ser modificado. El intento lanza `ClosedRecordMutationException`.
- **Por qué es vital:** Garantiza la trazabilidad auditora legal. El dato que se usó para calcular la nómina debe ser idéntico al que existe en BD en el momento de cualquier auditoría del Ministerio de Trabajo.

### 2. Invariantes del Agregado: DeviceRegistry

**Invariante de Unicidad de Dispositivo Activo por OrgUnit y Tipo (Device Uniqueness)**

- **Regla:** Para cada combinación (`org_unit_id`, `device_type`), solo puede existir un `ClockingDevice` en estado `ACTIVE` configurado como dispositivo `PRIMARIO`. Pueden existir N dispositivos `SECONDARY` o `BACKUP`.
- **Por qué es vital:** Dos dispositivos primarios activos del mismo tipo en la misma `OrgUnit` con configuraciones diferentes producirían resultados inconsistentes.

**Invariante de Firma Digital en Marcaciones de Dispositivo (Device Signature Integrity)**

- **Regla:** Todo `TimeEntry` originado desde un `ClockingDevice` físico (`source = KIOSK` o `BIOMETRIC_READER`) debe incluir una firma digital válida generada con la clave privada del dispositivo. Un `TimeEntry` de estos canales sin firma válida es INVÁLIDO y no puede persistirse.
- **Por qué es vital:** Impide que actores maliciosos inyecten marcaciones falsas en la API simulando ser un kiosco.

---

## Estructura de Aggregates y Entidades: BC Tiempos y Marcaciones

El BC-TM se organiza en tres Aggregates con responsabilidades claramente delimitadas. La capa de dominio es pura: ninguna clase contiene anotaciones de infraestructura (JPA, Spring).

### Agregado 14: AttendanceLedger (Libro Mayor de Asistencia)

Corazón del BC-TM. Representa el diario de hechos reales de un colaborador en un día calendario específico. Es la fuente de verdad para el cálculo de nómina.

**Root Entity: AttendanceLedger**
- Contenedor diario de todos los eventos de marcación. Define el contexto temporal (`work_date`) y el estado de completitud.
- **Responsabilidad:** Garantizar la Invariante de Paridad y la Invariante de Inmutabilidad Post-Cierre antes de transicionar a `CLOSED`.

**Entity: TimeEntry**
- Evento atómico e inmutable de marcación. Cada interacción del colaborador crea un nuevo `TimeEntry`. NUNCA se modifica; las correcciones crean uno nuevo.
- **Responsabilidad:** Capturar el tiempo NTP, el contexto geográfico y la identidad del marcador. Mantener la trazabilidad del canal y dispositivo.

**Entity: TimeDeviationRecord**
- Delta calculada entre lo planificado (`AssignedShift` de BC-SCH) y lo real (`TimeEntry`). Es el único documento modificable por el MSS durante WF-TM02.
- **Responsabilidad:** Mantener el ciclo de vida de la excepción (`PENDING → APPROVED/REJECTED/OVERRIDDEN`) y registrar la auditoría de la decisión.

**Value Object: PunchContext**
- Snapshot inmutable del contexto en el momento exacto de la marcación.
- **Atributos:** `device_id`, `source_channel` (MOBILE, KIOSK, WEB, MANUAL), `ip_address`, `device_signature` (hash de firma digital).

**Value Object: GeoValidationSnapshot** **[GEO-02/03 ACTUALIZADO]**
- Resultado del cruce entre las coordenadas GPS del dispositivo y el departamento (`Extension` ENUM) configurado en la `OrgUnit` de BC-01 Core.
- **Atributos:** `latitude`, `longitude`, `accuracy_meters`, `org_extension_snapshot` (ENUM: SCZ | LP | CB | OR | PT | TJ | BE | PA — copia del `extension` de la OrgUnit en el momento de la marcación, para trazabilidad histórica incluso si Core actualiza el registro), `is_within_extension` (BOOLEAN — resultado del chequeo departamental), `geo_status` (INSIDE, OUTSIDE_FENCE, REMOTE_AUTHORIZED, NO_GPS).

**Value Object: WorkedHoursSummary**
- Resultado calculado y consolidado del día, generado por el CRON de WF-TM03. Inmutable una vez que el Ledger cierra.
- **Atributos:** `regular_hours` (DECIMAL), `overtime_hours` (DECIMAL), `night_hours` (DECIMAL), `holiday_hours` (DECIMAL), `deducted_break_minutes` (INT), `net_payable_hours` (DECIMAL).

**Value Object: ExceptionAuditEntry**
- Registro inmutable de cada acción tomada sobre un `TimeDeviationRecord`.
- **Atributos:** `actor_id`, `action_timestamp`, `previous_status`, `new_status`, `reason_note`, `secondary_approver_id` (opcional).

### Agregado 15: DeviceRegistry (Registro de Dispositivos)

Gestiona el ciclo de vida de los dispositivos físicos y lógicos de marcación y las credenciales biométricas de los colaboradores.

**Root Entity: ClockingDevice**
- Representa un dispositivo físico o lógico (canal móvil configurado) autorizado para recibir marcaciones. Tiene identidad criptográfica única.
- **Responsabilidad:** Mantener la Invariante de Unicidad de Dispositivo Primario y gestionar el ciclo de vida del par de claves criptográficas.

**Entity: BiometricEnrollment**
- Vincula el template biométrico normalizado (nunca la imagen raw) de un colaborador con un `ClockingDevice`.
- **Responsabilidad:** Controlar que un colaborador tenga máximo un template `ACTIVE` por tipo biométrico y gestionar la revocación automática al desvincularse.

**Entity: DeviceAuditLog**
- Registro inmutable de todos los eventos del ciclo de vida del dispositivo: provisioning, configuraciones, sincronizaciones, heartbeats y revocaciones.

**Value Object: DeviceCapabilities**
- **Atributos:** `supports_fingerprint`, `supports_facial`, `supports_nfc`, `supports_qr` (BOOLEAN), `firmware_version` (VARCHAR), `public_key_pem` (TEXT).

**Value Object: DeviceHeartbeat**
- **Atributos:** `last_seen_at` (TIMESTAMP), `battery_level` (INT 0-100), `sync_status` (SYNCED, PENDING_SYNC, OUT_OF_SYNC), `enrolled_employees_count` (INT).

**Value Object: PunchAttemptLog**
- **Atributos:** `attempted_at` (TIMESTAMP), `relationship_id` (UUID), `auth_result` (SUCCESS, BIOMETRIC_FAIL, REVOKED, FRAUD_DETECTED), `security_incident` (BOOLEAN).

### Agregado 16: TimesheetPeriod (Periodo de Consolidación)

Contenedor de cierre de un periodo de tiempo (semanal, quincenal, mensual) y gestor de la transmisión formal hacia BC-05 (Payroll). Es el "sobre sellado" que BC-05 recibe via Message Broker.

**Root Entity: TimesheetPeriod**
- **Responsabilidad:** Garantizar que el 100% de los `AttendanceLedger` del periodo estén `CLOSED` antes de publicar `ATTENDANCE_PERIOD_CLOSED`. Mantener la inmutabilidad una vez cerrado.

**Entity: DailyConsolidationSummary**
- Resumen calculado por el CRON para un `work_date` específico dentro del periodo. Agrega `WorkedHoursSummary` de todos los colaboradores de la `OrgUnit`.

**Entity: PayrollHandoffPackage**
- El paquete de datos final generado al cerrar el `TimesheetPeriod`. Contiene el resumen por empleado validado, listo para BC-05. Una vez creado, es inmutable.
- **Responsabilidad:** Ser el contrato formal entre BC-TM y BC-05. BC-05 solo lee este paquete vía el evento del Message Broker; nunca accede directamente a los `AttendanceLedger` individuales.

**Value Object: PeriodBoundary**
- **Atributos:** `period_start` (DATE), `period_end` (DATE), `period_type` (WEEKLY, BIWEEKLY, MONTHLY), `grace_period_end` (TIMESTAMP según P-TM34).

**Value Object: EmployeeHandoffRecord**
- **Atributos:** `relationship_id`, `regular_hours_total`, `overtime_hours_total`, `night_hours_total`, `holiday_hours_total`, `unjustified_absences_count`, `remote_work_days`, `data_quality_flag`.

---

## Diccionario de Datos Profundo: BC Tiempos y Marcaciones

Persistencia completa para generar las migraciones de base de datos.

### Agregado 14: AttendanceLedger

| **Entidad / VO** | **Campo** | **Tipo de Dato** | **Restricciones / Lógica** |
| --- | --- | --- | --- |
| AttendanceLedger (Root) | ledger_id | UUID | PK. Generado por el servidor. |
| | tenant_id | UUID | FK a Tenant (BC-01). Partición multi-tenant. |
| | relationship_id | UUID | FK a Relationship (BC-01). NOT NULL. |
| | work_date | DATE | Día calendario de la jornada. NOT NULL. |
| | shift_id | UUID | FK a AssignedShift (BC-SCH). NULL si no hay turno asignado (guardia). |
| | status | ENUM | OPEN, PENDING_REVIEW, CLOSED. Default: OPEN. |
| | is_finalized | BOOLEAN | TRUE solo cuando el periodo fue cerrado y transmitido. Inmutable una vez TRUE (P-TM33). |
| | remote_work | BOOLEAN | TRUE si el día fue autorizado como jornada remota (WF-TM05). Default: FALSE. |
| | remote_work_auth_id | UUID | FK a RemoteWorkAuth. NULL si remote_work = FALSE. |
| | created_at | TIMESTAMP | Hora del servidor (NTP). NOT NULL. |
| | closed_at | TIMESTAMP | Hora de cierre. NULL si status != CLOSED. |
| TimeEntry | entry_id | UUID | PK. |
| | ledger_id | UUID | FK a AttendanceLedger. NOT NULL. |
| | punch_time | TIMESTAMP | Hora del servidor NTP. NOT NULL. Invariante: no puede ser futuro. |
| | punch_type | ENUM | PUNCH_IN, PUNCH_OUT, MEAL_START, MEAL_END, MANUAL_CORRECTION, DUPLICATE_REVIEW. |
| | source | ENUM | MOBILE, KIOSK, BIOMETRIC_READER, WEB, MANUAL. NOT NULL. |
| | device_id | UUID | FK a ClockingDevice (Agg.15). NULL si source = MANUAL o WEB. |
| | device_signature | TEXT | Firma digital del dispositivo. NULL si MOBILE/WEB. NOT NULL si KIOSK/BIOMETRIC_READER. |
| | is_retroactive | BOOLEAN | TRUE si fue creado por un MSS para una fecha pasada (P-TM32). Default: FALSE. |
| | retroactive_approver_id | UUID | FK a User. NOT NULL si is_retroactive = TRUE. |
| | corrects_entry_id | UUID | FK a TimeEntry (self-ref). Para entradas de corrección, referencia al entry original. |
| | fraud_flag | BOOLEAN | TRUE si el motor anti-fraude detectó anomalía (P-TM30). Default: FALSE. |
| PunchContext (VO) | ip_address | VARCHAR(45) | IPv4 o IPv6. NOT NULL para canales web/móvil. |
| | user_agent | TEXT | User-agent del cliente. Para auditoría de canal. |
| GeoValidationSnapshot (VO) **[GEO-03]** | latitude | DECIMAL(9,6) | Coordenada GPS del dispositivo. NULL si source = KIOSK/WEB. |
| | longitude | DECIMAL(9,6) | Coordenada GPS del dispositivo. NULL si source = KIOSK/WEB. |
| | accuracy_meters | DECIMAL(8,2) | Precisión GPS reportada por el dispositivo. |
| | org_extension_snapshot | ENUM | SCZ, LP, CB, OR, PT, TJ, BE, PA. Copia del `extension` de la OrgUnit en el momento de la marcación. Trazabilidad histórica. **[GEO-03 REEMPLAZA zone_code_snapshot]** |
| | is_within_extension | BOOLEAN | TRUE si las coordenadas GPS corresponden al departamento `org_extension_snapshot`. NOT NULL para canal móvil. **[GEO-03 REEMPLAZA is_within_zone]** |
| | geo_status | ENUM | INSIDE, OUTSIDE_FENCE, REMOTE_AUTHORIZED, NO_GPS. |
| TimeDeviationRecord | deviation_id | UUID | PK. |
| | ledger_id | UUID | FK a AttendanceLedger. NOT NULL. |
| | deviation_type | ENUM | LATE_IN, EARLY_OUT, OVERTIME, NO_SHOW, GEO_VIOLATION, MISSING_PUNCH, UNAUTHORIZED_ABSENCE. |
| | deviation_minutes | INT | Duración de la desviación en minutos. Puede ser negativo (salida temprana). |
| | resolution_status | ENUM | PENDING, APPROVED, REJECTED, OVERRIDDEN_BY_MANAGER, AUTO_CLOSED_AS_UNJUSTIFIED. |
| | detected_at | TIMESTAMP | Momento en que el sistema creó la desviación. |
| | resolved_at | TIMESTAMP | Momento de la resolución. NULL si PENDING. |
| | resolved_by | UUID | FK a User (MSS/Analista). NULL si PENDING o AUTO_CLOSED. |
| | reason_note | TEXT | Nota obligatoria del MSS al resolver. Min 20 chars si OVERRIDDEN/APPROVED. |
| | secondary_approver_id | UUID | FK a User. Para retroactividad >48h (P-TM32). |
| WorkedHoursSummary (VO) | regular_hours | DECIMAL(6,2) | Horas ordinarias dentro del turno contratado. |
| | overtime_hours | DECIMAL(6,2) | Horas extras aprobadas. |
| | night_hours | DECIMAL(6,2) | Horas entre 22:00 y 06:00 (recargo 25% LGT Bolivia). |
| | holiday_hours | DECIMAL(6,2) | Horas en día feriado oficial (recargo 100% LGT Bolivia). |
| | deducted_break_minutes | INT | Minutos de descanso deducidos automáticamente. |
| | net_payable_hours | DECIMAL(6,2) | CALCULADO: regular + overtime + night + holiday – (deducted/60). |
| | calculated_at | TIMESTAMP | Momento del cálculo por el CRON. Inmutable post-CLOSED. |

### Agregado 15: DeviceRegistry

| **Entidad / VO** | **Campo** | **Tipo de Dato** | **Restricciones / Lógica** |
| --- | --- | --- | --- |
| ClockingDevice (Root) | device_id | UUID | PK. |
| | tenant_id | UUID | FK a Tenant (BC-01). |
| | org_unit_id | UUID | FK a OrgUnit (BC-01). NOT NULL. |
| | serial_number | VARCHAR(100) | Número de serie del hardware. UNIQUE dentro del tenant. |
| | device_type | ENUM | KIOSK, BIOMETRIC_READER, NFC_TERMINAL, MOBILE_APP_CHANNEL. |
| | device_role | ENUM | PRIMARY, SECONDARY, BACKUP. Invariante de Unicidad aplica a PRIMARY. |
| | status | ENUM | PROVISIONING, ACTIVE, SUSPENDED, DECOMMISSIONED. |
| | installed_at | TIMESTAMP | Fecha de activación. NOT NULL si status = ACTIVE. |
| | decommissioned_at | TIMESTAMP | Fecha de baja. NULL si no está dado de baja. |
| DeviceCapabilities (VO) | supports_fingerprint | BOOLEAN | Default: FALSE. |
| | supports_facial | BOOLEAN | Default: FALSE. |
| | supports_nfc | BOOLEAN | Default: FALSE. |
| | supports_qr | BOOLEAN | Default: FALSE. |
| | firmware_version | VARCHAR(50) | Versión del firmware instalado. |
| | public_key_pem | TEXT | Clave pública RSA-2048 del dispositivo para verificar firmas. |
| DeviceHeartbeat (VO) | last_seen_at | TIMESTAMP | Último latido recibido. |
| | battery_level | SMALLINT | 0-100. NULL si kiosco con corriente alterna. |
| | sync_status | ENUM | SYNCED, PENDING_SYNC, OUT_OF_SYNC. |
| | enrolled_employees_count | INT | Número de empleados con templates activos en el dispositivo. |
| BiometricEnrollment | enrollment_id | UUID | PK. |
| | device_id | UUID | FK a ClockingDevice. NOT NULL. |
| | relationship_id | UUID | FK a Relationship (BC-01). NOT NULL. |
| | biometric_type | ENUM | FINGERPRINT, FACIAL. UNIQUE por (relationship_id, biometric_type) si status = ACTIVE. |
| | template_hash | VARCHAR(128) | Hash SHA-512 del template normalizado. El template raw NUNCA se almacena. |
| | template_quality_score | DECIMAL(4,2) | Score de calidad de la captura (0.00 – 1.00). |
| | status | ENUM | ACTIVE, SUSPENDED, REVOKED. |
| | enrolled_at | TIMESTAMP | Fecha de enrolamiento. NOT NULL. |
| | revoked_at | TIMESTAMP | Fecha de revocación. NULL si status != REVOKED. |
| | revocation_reason | ENUM | EMPLOYEE_OFFBOARDING, FRAUD_DETECTED, EMPLOYEE_REQUEST, DEVICE_DECOMMISSION. NULL si no revocado. |
| PunchAttemptLog | attempt_id | UUID | PK. |
| | device_id | UUID | FK a ClockingDevice. NOT NULL. |
| | attempted_at | TIMESTAMP | Hora del servidor del intento. NOT NULL. |
| | relationship_id | UUID | FK a Relationship. NOT NULL. |
| | auth_method | ENUM | FINGERPRINT, FACIAL, PIN, NFC, QR. |
| | auth_result | ENUM | SUCCESS, BIOMETRIC_FAIL, REVOKED_CREDENTIAL, DEVICE_SIGNATURE_FAIL, FRAUD_DETECTED. |
| | security_incident | BOOLEAN | TRUE si FRAUD_DETECTED o 3+ fallos consecutivos del mismo colaborador. Default: FALSE. El dispositivo NO se bloquea por este flag. |
| | incident_escalated_to | UUID | FK a User (MSS/Seguridad). NOT NULL si security_incident = TRUE. |

### Agregado 16: TimesheetPeriod

| **Entidad / VO** | **Campo** | **Tipo de Dato** | **Restricciones / Lógica** |
| --- | --- | --- | --- |
| TimesheetPeriod (Root) | period_id | UUID | PK. |
| | tenant_id | UUID | FK a Tenant (BC-01). |
| | org_unit_id | UUID | FK a OrgUnit (BC-01). NOT NULL. |
| | period_type | ENUM | WEEKLY, BIWEEKLY, MONTHLY. Configurado a nivel de Tenant. |
| | period_start | DATE | Primer día del periodo. NOT NULL. |
| | period_end | DATE | Último día del periodo. Invariante: > period_start. NOT NULL. |
| | grace_period_end | TIMESTAMP | Calculado: period_end + días hábiles según P-TM34. NOT NULL. |
| | status | ENUM | OPEN, IN_GRACE_PERIOD, CLOSING, CLOSED, TRANSMITTED. |
| | closed_at | TIMESTAMP | Momento del cierre. NULL si status != CLOSED. |
| | closed_by | UUID | FK a User. NULL si cierre fue automático por CRON. |
| | closure_type | ENUM | MANUAL (por MSS/Analista) o AUTO (por CRON al vencer P-TM34). |
| | payroll_event_emitted_at | TIMESTAMP | Momento en que se publicó ATTENDANCE_PERIOD_CLOSED al Message Broker. NULL hasta entonces. |
| DailyConsolidationSummary | summary_id | UUID | PK. |
| | period_id | UUID | FK a TimesheetPeriod. NOT NULL. |
| | work_date | DATE | Día del resumen. NOT NULL. |
| | total_scheduled | INT | Número de empleados con turno asignado ese día. |
| | total_attended | INT | Número de empleados con al menos un PUNCH_IN. |
| | total_no_shows | INT | Empleados con turno asignado y cero marcaciones. |
| | total_exceptions_pending | INT | Excepciones PENDING al momento del CRON. |
| | total_regular_hours | DECIMAL(8,2) | Suma de regular_hours de todos los WorkedHoursSummary del día. |
| | total_overtime_hours | DECIMAL(8,2) | Suma de overtime_hours aprobadas del día. |
| | total_night_hours | DECIMAL(8,2) | Suma de night_hours del día. |
| | calculated_at | TIMESTAMP | Momento en que el CRON calculó este resumen. |
| PayrollHandoffPackage | handoff_id | UUID | PK. |
| | period_id | UUID | FK a TimesheetPeriod. UNIQUE (solo un handoff por periodo). NOT NULL. |
| | generated_at | TIMESTAMP | Momento de generación. Inmutable. |
| | checksum | VARCHAR(128) | Hash SHA-512 del payload completo para verificación de integridad. |
| EmployeeHandoffRecord (VO) | relationship_id | UUID | FK a Relationship. NOT NULL. PK compuesto con handoff_id. |
| | regular_hours_total | DECIMAL(8,2) | Acumulado del periodo de horas ordinarias. NOT NULL. |
| | overtime_hours_total | DECIMAL(8,2) | Acumulado del periodo de horas extras aprobadas. NOT NULL. |
| | night_hours_total | DECIMAL(8,2) | Acumulado del periodo de horas nocturnas. NOT NULL. |
| | holiday_hours_total | DECIMAL(8,2) | Acumulado del periodo de horas en festivos. NOT NULL. |
| | unjustified_absences | INT | Número de días con ausencia injustificada. Impacta descuento en nómina. |
| | remote_work_days | INT | Número de días trabajados en modalidad remota autorizada. |
| | data_quality_flag | ENUM | COMPLETE, PARTIAL_AUTO_CLOSED. Indica si hubo auto-cierres por vencimiento de ventana. |

### Tabla Auxiliar: RemoteWorkAuth (Autorización de Trabajo Remoto)

Entidad de soporte del BC-TM consultada por `AttendanceLedger` y `TimesheetPeriod`. No es un Aggregate Root.

| **Campo** | **Tipo de Dato** | **Restricciones / Lógica** |
| --- | --- | --- |
| auth_id | UUID | PK. |
| tenant_id | UUID | FK a Tenant (BC-01). |
| relationship_id | UUID | FK a Relationship (BC-01). NOT NULL. |
| requested_by | UUID | FK a User (quien solicitó — el propio colaborador o el MSS). |
| approved_by | UUID | FK a User (MSS). NOT NULL si status = APPROVED. |
| remote_type | ENUM | TELETRABAJO, COMISION_SERVICIO, CONTINGENCIA. |
| auth_date_start | DATE | Primer día autorizado. NOT NULL. |
| auth_date_end | DATE | Último día autorizado. Invariante: >= auth_date_start. |
| requires_location_breadcrumb | BOOLEAN | Si se exige registro GPS periódico. TRUE para TELETRABAJO. |
| requires_photo_evidence | BOOLEAN | Si se exige foto del lugar de trabajo. TRUE para CONTINGENCIA. |
| status | ENUM | PENDING, APPROVED, REJECTED, REVOKED. |
| created_at | TIMESTAMP | Hora de creación de la solicitud. NOT NULL. |
| approved_at | TIMESTAMP | Hora de aprobación. NULL si no aprobado. |

> **Nota v1.2:** El campo `geo_tolerance_km` de v1.1 ha sido eliminado de `RemoteWorkAuth`. La validación geográfica ahora se basa exclusivamente en el `Extension` ENUM departamental de BC-01 Core para los modos `ESTÁNDAR` y `TELETRABAJO`. Para `COMISION_SERVICIO` y `CONTINGENCIA` no existe restricción de zona; solo se registran las coordenadas para trazabilidad. **[GEO-02]**

---

## Mapa de Dependencias de Datos (Context Mapping)

> **📌 Recordatorio: Upstream vs Downstream**
> Upstream → BC-TM CONSULTA datos de estos BCs (queries síncronas o suscripción a eventos). BC-TM nunca escribe en sus tablas.
> Downstream → BC-TM PUBLICA eventos al Message Broker. Los BCs downstream los consumen cuando están disponibles.

### Dependencias Upstream (BC-TM consulta datos de…)

| **Bounded Context Origen** | **Dato Consumido** | **Cuándo / Para qué** | **Tipo de Integración** |
| --- | --- | --- | --- |
| BC-01 Core HR (Organization & Workforce) | `Relationship.status`, `Relationship.org_unit_id` | WF-TM01: Antes de crear un TimeEntry, verifica que el `relationship_id` esté `ACTIVE`. Un colaborador inactivo o desvinculado no puede marcar. | Query síncrona (Anti-Corruption Layer) |
| BC-01 Core HR (Organization & Workforce) | `OrgUnit.extension` (ENUM: SCZ, LP, CB, OR, PT, TJ, BE, PA — departamento boliviano gestionado por Core). **[GEO-01 ACTUALIZADO]** | WF-TM01: BC-TM recibe el `org_extension` y valida si las coordenadas GPS del dispositivo corresponden al departamento. BC-TM no almacena polígonos ni coordenadas propias de referencia; la fuente de verdad es el ENUM departamental de BC-01. | Query síncrona (ACL). BC-TM hace snapshot de `org_extension` en `GeoValidationSnapshot` como `org_extension_snapshot` para trazabilidad histórica incluso si Core actualiza el campo. |
| BC-01 Core HR (Person & Identity) | `Person.address_record` (domicilio para teletrabajo) | WF-TM05: Para definir el departamento de referencia en jornadas `TELETRABAJO` (P-TM28). | Query síncrona (ACL) |
| BC-01 Core HR (Calendar & Holidays) | `CalendarHoliday.holiday_date`, `holiday_type` | WF-TM03 (CRON): Para clasificar horas trabajadas en feriados y aplicar el recargo del 100% en `WorkedHoursSummary`. | Query síncrona (ACL) |
| BC-SCH Scheduling (Aggregate 12: RosterManagement) | `AssignedShift.expected_start`, `expected_end`, `shift_type`, `shift_id` | WF-TM01 y WF-TM03: Para comparar la marcación real contra el turno teórico y calcular `TimeDeviationRecord`. | Query síncrona (ACL). BC-TM define su propio `ShiftReference` local con solo los campos necesarios. |
| BC-04 Accruals (Time Off & Seniority) | `LeaveTransaction.leave_date`, `leave_type`, `status` | WF-TM02: Al justificar una ausencia, verifica si existe una `LeaveTransaction` aprobada para esa fecha, evitando doble justificación. | Query síncrona (ACL) |

**Eventos de BC-01 Core a los que BC-TM se suscribe:**

| **Evento de Core** | **Acción en BC-TM** | **Motivo** |
| --- | --- | --- |
| `EMPLOYEE_DEACTIVATED` | Revoca automáticamente todos los `BiometricEnrollment` ACTIVE del `relationship_id`. | Un colaborador desvinculado no debe poder marcar en ningún dispositivo. |
| `ORG_UNIT_EXTENSION_UPDATED` **[GEO-05]** | Invalida el caché local del `org_extension` de la OrgUnit y fuerza re-consulta en la próxima marcación. | El departamento asignado a la OrgUnit cambió en Core; BC-TM debe reflejarlos para la validación geográfica. |
| `HOLIDAY_CALENDAR_PUBLISHED` | Actualiza la referencia de feriados para el CRON de consolidación del siguiente ciclo. | El cálculo de horas festivas depende del calendario de Core. |

### Dependencias Downstream (BC-TM publica eventos para…)

| **Bounded Context Destino** | **Evento Publicado / Payload** | **Cuándo** | **Tipo de Integración** |
| --- | --- | --- | --- |
| BC-05 Financial & Payroll | `ATTENDANCE_PERIOD_CLOSED` / Payload: `PayrollHandoffPackage` con `EmployeeHandoffRecord[]` | WF-TM03: Al cerrar un `TimesheetPeriod`. BC-05 NUNCA lee directamente los `AttendanceLedger`. | Evento de dominio asíncrono (Message Broker) |
| BC-01 Core HR (ESS/MSS Notifications) | `PUNCH_ANOMALY_DETECTED` / Payload: `ledger_id`, `relationship_id`, `deviation_type`, `detected_at` | WF-TM01: Al detectar `LATE_IN` o `GEO_VIOLATION` en tiempo real. | Evento asíncrono (Message Broker) |
| BC-01 Core HR (ESS/MSS Notifications) | `EXCEPTION_AUTO_CLOSED` / Payload: `deviation_id`, `relationship_id`, `closure_reason`, `financial_impact` | WF-TM02: Cuando una excepción vence sin resolución (P-TM31, P-TM34). | Evento asíncrono (Message Broker) |
| BC-01 Core HR (Security & Audit) | `SECURITY_PUNCH_INCIDENT` / Payload: `SecurityIncidentRecord` con `device_id`, `relationship_id`, `incident_type`, `attempted_at` | WF-TM04: Cuando `PunchAttemptLog` registra `security_incident = TRUE`. El dispositivo sigue operativo. | Evento asíncrono (Message Broker) |
| BC-SCH Scheduling (Feedback de Asistencia) | `ATTENDANCE_SUMMARY_FOR_ROSTER` / Payload: `work_date`, `relationship_id`, `attendance_rate_last_30d` | Diario (CRON): métricas de asistencia para el motor de recomendación de turnos de BC-SCH. | Evento asíncrono (Message Broker) |

### Reglas de Anti-Corruption Layer (ACL)

- **Traducción de Identidades:** BC-TM usa `relationship_id` como una referencia opaca (UUID). `AttendanceLedger` no sabe nada del modelo de `Person` ni de `Contract`; solo conoce el `relationship_id` como llave foránea. Esto garantiza coherencia con los cambios en `PersonIdentity` (campos `marital_status`, `profession_title`) y `EmploymentAgreement` (`employment_cond`) del `Resumen de Cambios` — esos campos pertenecen a BC-01 y BC-02 respectivamente y BC-TM no los necesita para su operación.

- **Contrato del AssignedShift:** BC-TM no importa clases de BC-SCH. Define su propia representación interna `ShiftReference` con solo los campos necesarios: `shift_id`, `expected_start`, `expected_end`. Si BC-SCH cambia su modelo, solo se actualiza el adaptador de la ACL.

- **Contrato del GeoExtension:** **[GEO-02 NUEVO]** BC-TM no importa el modelo de `OrgUnit` de BC-01. Define su propio `GeoExtensionReference` con el campo: `org_extension` (ENUM: SCZ | LP | CB | OR | PT | TJ | BE | PA). Si Core agrega un nuevo departamento al ENUM, solo se actualiza el adaptador de la ACL y el ENUM local de BC-TM. El `org_extension` se guarda en cada `GeoValidationSnapshot` como `org_extension_snapshot` para trazabilidad histórica incluso si la asignación cambia después.

- **Eventos Publicados como Contratos Versionados:** El `PayrollHandoffPackage` es el contrato de integración con BC-05. Está versionado (v1.0, v2.0). BC-05 se suscribe a una versión específica. El versionado permite que BC-TM evolucione su modelo interno sin romper la integración con nómina. Coherente con la adición de `SEGUNDO_AGUINALDO` en BC-05 (`Resumen de Cambios`) — ese beneficio se calcula en BC-05 a partir del `PayrollHandoffPackage`, no en BC-TM.

- **Idempotencia en Consumo de Eventos:** BC-TM garantiza que consumir el mismo evento de entrada dos veces produce el mismo resultado.

---

*PeopleCoreIA · BC Tiempos y Marcaciones · v1.2.0*
*Arquitectura Hexagonal · Domain-Driven Design · Java 21 + Spring Boot*
*Normativa: Ley General del Trabajo Bolivia · D.S. 21060 · SMN Bs 3.300 (2026)*
