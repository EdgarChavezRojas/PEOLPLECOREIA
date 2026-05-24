**Modulo Scheduling Oficial**

**Workflows Operativos del Módulo de Scheduling**

**1. Workflow de Creación y Publicación de Mallas (Rostering)**

Este es el proceso iterativo donde un administrador o gerente de tienda/facultad planifica la semana o mes.

- **Actor Principal:** Manager (MSS).
- **Propósito:** Generar una grilla de turnos (SchedulePlan) libre de conflictos legales y operativos.
- **Flujo Paso a Paso:**
  - **Inicialización:** El MSS selecciona la OrgUnit y el periodo de tiempo (ej. Semana 42). El sistema crea un SchedulePlan en estado DRAFT.
  - **Carga de Plantilla (Opcional):** El sistema copia el patrón base de la semana anterior o una plantilla estandarizada (ej. "Turno Mañana / Tarde").
  - **Asignación de Turnos:** El MSS asigna empleados a los bloques horarios.
  - **Intercepción del Constraint Engine:** En tiempo real, por cada asignación, el motor verifica las *Hard/Soft Constraints* (Tope de horas, cruce con vacaciones, documentos expirados).
  - **Resolución de Conflictos:** Si hay ConstraintViolation tipo HARD, la celda se pinta de rojo y bloquea la publicación. El MSS debe reasignar.
  - **Aprobación de Presupuesto (Soft):** El sistema calcula el costo proyectado (incluyendo recargos nocturnos o dominicales) y valida contra el BudgetFunding.
  - **Publicación:** El estado cambia a PUBLISHED.
  - **Notificación (ESS):** Se dispara un evento asíncrono (SCHEDULE\_PUBLISHED) que envía notificaciones Push a todos los empleados afectados.

**2. Workflow de Movilidad de Turnos (Shift Swapping & Open Shifts)**

Crucial para dar flexibilidad a la operación (especialmente en Retail) sin recargar a Recursos Humanos.

- **Actor Principal:** Trabajador (ESS) y Manager (MSS).
- **Propósito:** Permitir que los empleados intercambien turnos o se postulen a turnos huérfanos, respetando las reglas de cumplimiento.
- **Flujo Paso a Paso (Intercambio - Swap):**
  - **Solicitud:** Empleado A selecciona su turno en la App (ESS) y solicita un cambio con el Empleado B.
  - **Aceptación de Par:** Empleado B recibe notificación y acepta.
  - **Validación del Motor:** El sistema verifica de forma invisible si el Empleado B no excede sus 48h/40h semanales al tomar este nuevo turno y si tiene los *Skills* necesarios.
  - **Aprobación MSS:** Si pasa la validación, la solicitud llega al Gerente.
  - **Reasignación:** El gerente aprueba. El AssignedShift cambia de relationship\_id y notifica a ambos.
- **Flujo Paso a Paso (Turnos Abiertos - Open Shifts):**
  - El MSS publica un turno sin asignar ("Turno Huérfano").
  - Empleados elegibles (filtrados por el Constraint Engine) lo ven en la App y hacen "Bid" (se postulan).
  - El MSS elige al candidato óptimo o el sistema lo auto-asigna al primero que cumpla las reglas.

**3. Workflow de Marcación y Validación en Tiempo Real (Time Tracking)**

Es el momento de la verdad donde el mundo físico se conecta con el sistema.

- **Actor Principal:** Trabajador (ESS / Kiosco).
- **Propósito:** Registrar el TimeEntry garantizando identidad y ubicación.
- **Flujo Paso a Paso:**
  - **Intento de Marcación:** El empleado abre la App y presiona "Punch-In".
  - **Captura de Contexto:** La App captura la hora oficial del servidor (NTP), no la del teléfono, y las coordenadas GPS.
  - **Validación de Geocerca (Geo-Fencing):** Compara el GPS con el polígono/coordenadas del OrgUnit. Si excede la tolerancia (ej. 100 metros), genera una excepción.
  - **Validación de Identidad (Anti-Fraude):** Si la política lo exige, solicita biometría facial o de huella.
  - **Creación del Registro:** Se consolida el TimeEntry y se vincula al AttendanceRecord del día.
  - **Cruce Téorico/Real:** El sistema compara inmediatamente la hora marcada con la hora esperada (expected\_start). Si marca tarde, dispara una alerta interna de tardanza.

**4. Workflow de Conciliación y Cierre (Timesheet Approval)**

El paso final antes de que la información pase al módulo financiero y de planillas.

- **Actor Principal:** Manager (MSS) o Analista de Planillas.
- **Propósito:** Limpiar las excepciones, justificar ausencias y aprobar horas extras.
- **Flujo Paso a Paso:**
  - **Consolidación Diaria/Semanal:** Un CRON job nocturno barre todos los AttendanceRecord vs los AssignedShift.
  - **Detección de Desviaciones (TimeDeviation):**
    - *No-Show:* Turno asignado, cero marcaciones.
    - *Tardanza (Late-In):* Marcó después de la tolerancia (ej. +10 mins).
    - *Salida Temprana (Early-Out):* Marcó antes de la hora.
    - *Sobretiempo (Overtime):* Marcó salida 2 horas después.
  - **Revisión del Manager:** El MSS entra a la grilla de excepciones. Puede:
    - Aprobar el Overtime (se convertirá en pago).
    - Rechazar el Overtime (se ajusta la hora de salida a la hora teórica).
    - Justificar una ausencia (conectar con una baja médica del BC 04).
  - **Cierre de Periodo:** Una vez que no hay excepciones sin resolver, el AttendanceRecord pasa a is\_closed = TRUE.
  - **Sincronización:** Se envían los totales validados al BC 05 (Financial) para el cálculo de nómina.

**Políticas** 
### **Políticas del Módulo de Scheduling (Operational Constraints)**
#### **Clúster de Asistencia y Marcación (Time & Attendance)**
**P19: Política de Tolerancia y Redondeo (Grace Period & Rounding)**

- **Propósito:** Estandarizar cómo se trata el tiempo fraccional para evitar micro-descuentos o micro-sobretiempos que complican la nómina.
- **Regla de Negocio:** \* Se otorga una **tolerancia de gracia de 5 a 10 minutos** (configurable por Tenant) para el Punch-In sin marcar tardanza.
  - Para el cálculo de pago, las marcaciones se **redondean al cuarto de hora más cercano** (Ej: Un Punch-In a las 08:06 se asume como 08:00; un Punch-Out a las 17:10 se asume como 17:00 para no pagar 10 mins extra).
- **Impacto en el Sistema:** Genera el estado LATE\_IN solo si se supera la tolerancia. Modifica el cálculo final del TimeDeviation.
#### **Clúster de Planificación y Salud Ocupacional (Rostering & Well-being)**
**P20: Política de Descanso Obligatorio entre Turnos (Anti-Clopening)**

- **Propósito:** Proteger la salud del trabajador evitando cierres a medianoche seguidos de aperturas a primera hora de la mañana.
- **Regla de Negocio:** Debe existir un **mínimo de 12 horas continuas de descanso** entre el expected\_end de un turno y el expected\_start del siguiente.
- **Impacto en el Sistema:** Funciona como un *Hard Constraint* (H1). El motor bloquea la publicación de la malla si se viola, salvo anulación expresa (Override) firmada digitalmente por el empleado.

**P21: Política de Pausas y Almuerzo Deductible (Meal Breaks)**

- **Propósito:** Cumplir con los descansos de jornada y no pagar horas que el empleado usa para motivos personales.
- **Regla de Negocio:** Todo AssignedShift mayor a **6 horas continuas** exige una pausa no remunerada obligatoria de mínimo 30 a 60 minutos.
- **Impacto en el Sistema:** Si el empleado olvida hacer el Punch-Out para su descanso, el sistema aplica una **deducción automática** del tiempo de pausa al calcular las horas efectivas, a menos que el gerente lo revierta manualmente.

**P24: Política de Límite de Días Consecutivos (Max Consecutive Days)**

- **Propósito:** Garantizar el descanso semanal establecido por la ley boliviana.
- **Regla de Negocio:** El motor de Scheduling bloquea automáticamente la asignación de un turno si el empleado ya acumula **6 días trabajados de forma consecutiva**. El séptimo día debe registrarse obligatoriamente como descanso o compensatorio.
- **Impacto en el Sistema:** Fuerte restricción (*Hard Constraint*) que impide sobrecargar a un empleado de lunes a domingo sin interrupción.
#### **Clúster de Control de Costos (Cost Control)**
**P22: Política de Aprobación Estricta de Sobretiempo (Strict Overtime)**

- **Propósito:** Evitar que los empleados acumulen horas extras innecesarias sin autorización del supervisor.
- **Regla de Negocio:** Cualquier tiempo trabajado que exceda los 15 minutos posteriores al fin de turno oficial se etiqueta como PENDING\_OVERTIME. **No suma al cálculo de nómina** hasta que el gerente cambie el estado a APPROVED.
- **Impacto en el Sistema:** En el cierre de planilla, si la desviación sigue pendiente, se descarta automáticamente del pago y se ajusta la salida a la hora teórica.

**P23: Política de Imputación Cruzada en Suplencias (Cross-Costing)**

- **Propósito:** Gestionar el gasto cuando un empleado de la Sucursal A cubre un turno en la Sucursal B.
- **Regla de Negocio:** El costo de las horas trabajadas en esa suplencia se imputa al CostCenter de la sucursal de destino, sin necesidad de transferir permanentemente al empleado en el Core.
- **Impacto en el Sistema:** El SchedulePlan genera un registro temporal de LaborCostSplit que el BC 05 consume exclusivamente para esas horas.

**P25: Política de Neutralidad de Costo en Intercambios (Shift Swap Cost Neutrality)**

- **Propósito:** Evitar que la flexibilidad de la autogestión de turnos genere costos imprevistos a la empresa.
- **Regla de Negocio:** El sistema permite el intercambio de turnos entre pares desde la App (ESS), pero el motor **rechaza automáticamente** la solicitud si el nuevo turno empuja a cualquiera de los dos empleados a un estado de Sobretiempo (OVERTIME) al superar sus 40h/48h semanales.
- **Impacto en el Sistema:** Garantiza que los intercambios operativos sean siempre financieramente neutros para el presupuesto (BudgetFunding).
### **Invariantes del Módulo de Scheduling**
#### **1. Invariantes del Agregado: RosterManagement (Planificación)**
Estas reglas protegen la coherencia de la grilla teórica antes de que la operación empiece.

- **Invariante de No Superposición de Turnos (No Overlapping Shifts):**
  - **Regla:** Un WorkerProfile no puede poseer dos AssignedShift cuyos bloques de tiempo (expected\_start y expected\_end) se crucen o superpongan, incluso si pertenecen a diferentes OrgUnit.
  - **Por qué es vital:** Un ser humano no puede estar en dos lugares o cubriendo dos roles al mismo tiempo. Romper esta regla destruiría el cálculo del costo de la nómina y generaría falsas horas extras.
- **Invariante de Contención Temporal (Plan Boundary Integrity):**
  - **Regla:** Las fechas y horas de cualquier AssignedShift deben estar estrictamente contenidas dentro de los límites de period\_start y period\_end de su SchedulePlan padre.
  - **Por qué es vital:** Evita que un gerente asigne por error un turno de la semana 43 en la malla de la semana 42, lo cual corrompería el cierre de planillas y la imputación de costos de ese periodo (LaborCostSplit).
#### **2. Invariantes del Agregado: TimeAndAttendance (Marcación Real)**
Estas reglas protegen la realidad física y temporal de los eventos de asistencia, previniendo fraudes o corrupción de datos.

- **Invariante de Secuencia Cronológica y Causalidad (Chronological Integrity & No Time-Travel):**
  - **Regla A:** Todo evento de salida (Punch-Out o Meal\_Start) debe tener un punch\_time estrictamente posterior a su evento de entrada correspondiente (Punch-In).
  - **Regla B:** Ningún TimeEntry puede registrar un punch\_time en el futuro relativo a la hora del servidor (NTP).
  - **Por qué es vital:** Impide que alteraciones manuales en el ESS o problemas con el reloj local de los dispositivos móviles generen duraciones de turnos negativas o marcaciones fraudulentas anticipadas.
- **Invariante de Paridad para el Cierre de Día (Attendance Closure Parity):**
  - **Regla:** Un AttendanceRecord no puede transicionar a su estado final (is\_closed = TRUE) si posee un número impar de eventos TimeEntry (es decir, una entrada sin salida) a menos que posea un TimeDeviation explícitamente aprobado por un MSS que justifique el hueco (ej. Abandono de trabajo).
  - **Por qué es vital:** Evita que se envíen registros abiertos al motor de nómina (BC 05). La nómina no sabe qué hacer con alguien que "entró pero nunca salió"; requiere un cierre matemático exacto para calcular el Total Ganado.
### **Estructura de Aggregates y Entidades: BC Scheduling**
#### **Agregado 12: RosterManagement (Planificación Teórica)**
Este agregado se encarga de la "promesa" de trabajo. Es el plano donde el gerente organiza los recursos humanos.

- **Root Entity: SchedulePlan**
  - Es el contenedor de la malla horaria. Define el horizonte temporal (semana/mes) y el ámbito (sucursal o unidad académica).
  - **Responsabilidad:** Validar que el plan esté completo y no viole el presupuesto asignado por el Core.
- **Entity: AssignedShift**
  - Representa un bloque de tiempo asignado a un colaborador específico.
  - **Responsabilidad:** Mantener la integridad de la asignación y verificar colisiones con otros turnos mediante el *Constraint Engine*.
- **Value Object: ConstraintViolation**
  - Registra cualquier regla rota (Soft/Hard) detectada durante la planificación.
  - **Atributos:** rule\_code, severity, message.
- **Value Object: ShiftMetadata**
  - Atributos adicionales del turno (ej. si requiere uniformes especiales o si es un turno de contingencia).
#### **Agregado 13: TimeAndAttendance (Ejecución Real)**
Este agregado se encarga de la "realidad". Registra los hechos físicos y calcula las desviaciones para la nómina.

- **Root Entity: AttendanceRecord**
  - Es el "sobre" que agrupa todas las interacciones de un empleado en un día calendario específico.
  - **Responsabilidad:** Garantizar la **Invariante de Paridad** (que cada entrada tenga su salida) antes de cerrar el día para pago.
- **Entity: TimeEntry**
  - Un evento atómico de marcación (fichaje).
  - **Responsabilidad:** Capturar con precisión el tiempo, la ubicación y la identidad del trabajador.
- **Value Object: GeoValidation**
  - Resultado del cruce entre el GPS del móvil y la geocerca de la OrgUnit.
  - **Atributos:** coordinates, accuracy, is\_within\_fence.
- **Value Object: TimeDeviation**
  - Representa la diferencia calculada entre el turno asignado y la marcación real.
  - **Atributos:** deviation\_type (Overtime, Late, Early), duration\_minutes, approval\_status.
### **Diccionario de Datos Profundo: BC Scheduling**
Aquí definimos la persistencia para que el equipo de desarrollo pueda crear las migraciones de base de datos.




**Agregado 12: RosterManagement**

|**Entidad / VO**|**Campo**|**Tipo de Dato**|**Restricciones / Lógica**|
| :- | :- | :- | :- |
|**SchedulePlan** (Root)|plan\_id|UUID|PK.|
||unit\_id|UUID|FK a OrgUnit (BC 01).|
||period\_start|DATE|Fecha de inicio del Roster.|
||period\_end|DATE|Fecha de fin (Invariante: > period\_start).|
||status|ENUM|DRAFT, PUBLISHED, ARCHIVED.|
||total\_projected\_cost|DECIMAL(15,2)|Cálculo basado en BudgetFunding (BC 05).|
|**AssignedShift**|shift\_id|UUID|PK.|
||plan\_id|UUID|FK a SchedulePlan.|
||relationship\_id|UUID|FK a Relationship (BC 01).|
||expected\_start|TIMESTAMP|Hora teórica de entrada.|
||expected\_end|TIMESTAMP|Hora teórica de salida.|
||shift\_type|ENUM|ORDINARY, ACADEMIC, MEAL\_BREAK (P21).|
||is\_active|BOOLEAN|Para bajas lógicas de turnos.|
|**ConstraintViolation**|violation\_id|UUID|PK.|
||shift\_id|UUID|FK a AssignedShift.|
||rule\_code|VARCHAR(20)|Ej: H1\_MAX\_HOURS, P24\_MAX\_DAYS.|
||severity|ENUM|HARD (Bloquea), SOFT (Alerta).|

**Agregado 13: TimeAndAttendance**

|**Entidad / VO**|**Campo**|**Tipo de Dato**|**Restricciones / Lógica**|
| :- | :- | :- | :- |
|**AttendanceRecord** (Root)|record\_id|UUID|PK.|
||relationship\_id|UUID|FK a Relationship (BC 01).|
||work\_date|DATE|El día al que pertenece la jornada.|
||is\_closed|BOOLEAN|**Invariante**: Solo TRUE si paridad es 100%.|
||status|ENUM|OPEN, PENDING\_REVIEW, CLOSED.|
|**TimeEntry**|entry\_id|UUID|PK.|
||record\_id|UUID|FK a AttendanceRecord.|
||punch\_time|TIMESTAMP|Hora del servidor (NTP).|
||punch\_type|ENUM|IN, OUT, MEAL\_START, MEAL\_END.|
||device\_id|VARCHAR(100)|ID del hardware/móvil usado.|
||source|ENUM|MOBILE, KIOSK, WEB, MANUAL.|
|**GeoValidation**|lat\_long|POINT/TEXT|Coordenadas reales al marcar.|
||is\_within\_fence|BOOLEAN|Según radio de 100m (P20).|
|**TimeDeviation**|deviation\_id|UUID|PK.|
||record\_id|UUID|FK a AttendanceRecord.|
||deviation\_type|ENUM|LATE\_IN, EARLY\_OUT, OVERTIME (P22).|
||minutes|INT|Duración de la desviación.|
||approval\_status|ENUM|PENDING, APPROVED, REJECTED.|
||approved\_by|UUID|FK a User (Analista/Gerente).|
### **Mapa de Dependencias de Datos (Context Mapping)**
Para que el desarrollador sepa qué servicios del Core consultar, definimos estas dependencias:

1. **Consulta de Persona/Relación (BC 01):** El AssignedShift necesita validar que el RelationshipID esté en estado ACTIVE.
1. **Consulta de Elegibilidad (BC 03):** Antes de guardar un AssignedShift, el sistema consulta el DigitalKardex para verificar documentos críticos (P7).
1. **Consulta de Saldos (BC 04):** Antes de permitir un turno, el sistema verifica que no haya una LeaveTransaction para esa fecha.
1. **Notificación de Pago (BC 05):** Cuando un AttendanceRecord se marca como CLOSED, se emite el evento ATTENDANCE\_READY\_FOR\_PAYROLL con el resumen de horas efectivas y desviaciones aprobadas.

