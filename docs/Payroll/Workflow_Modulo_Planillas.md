**SISTEMA DE RECURSOS HUMANOS**

**Documentación Técnica — Módulo de Planillas**

Workflow del Desarrollador — Contexto Bolivia

|**Versión**|1\.0|
| :- | :- |
|**Módulo**|Payroll / Gestión de Planillas|
|**Normativa aplicable**|LGT Bolivia, DS 21060, SMN 2026 (Bs 3.300)|


# **1. Introducción y alcance del módulo**
El módulo de Planillas (Payroll) es el componente del sistema de Recursos Humanos encargado de calcular, generar, revisar, aprobar y liquidar los haberes de todos los colaboradores de la organización, en estricto cumplimiento de la legislación laboral boliviana vigente.

Este documento describe el workflow completo que debe implementar el desarrollador a cargo del módulo, detallando cada fase del proceso, sus entradas, salidas, invariantes legales y dependencias con el módulo Core HR (BC: Workforce & Organization Master y BC: Financial & Social Compliance).

## **1.1. Dependencias con el Core HR**
El módulo de Planillas no opera de forma aislada. Su activación depende de eventos emitidos por el Bounded Context de Workforce & Organization Master. Los principales eventos que habilitan a un empleado en el motor de planillas son:

- ONBOARDING\_COMPLETED: el empleado posee identidad verificada (Person), vínculo laboral activo (Relationship), contrato aprobado con SoD y posición asignada con plaza presupuestada. Sin este evento, el empleado no aparece en ninguna planilla.
- CONTRACT\_ACTIVATED: el contrato ha superado las validaciones legales del motor de cumplimiento, incluyendo la verificación de que el haber básico no sea inferior al SMN vigente (Bs 3.300 para 2026).
- BANK\_ACCOUNT\_SYNCED: la cuenta bancaria del empleado ha sido validada con documentación de respaldo. Sin este evento, el sistema bloquea la generación del archivo de dispersión bancaria.

# **2. Workflow de implementación — Las 8 fases**

|**FASE 0**|<p>**Trigger desde el Core HR**</p><p>Prerequisito para cualquier cálculo de planilla</p>|
| :-: | :- |

Antes de iniciar cualquier proceso de planilla, el sistema verifica que el evento ONBOARDING\_COMPLETED haya sido emitido para el empleado. Este evento confirma que los cuatro pilares del Core HR están validados:

- Identidad (Person con CI verificado)
- Vínculo laboral activo (Relationship en estado Activo)
- Contrato aprobado (Contract con firma y SoD)
- Posición asignada con plaza presupuestada (Position con headcount disponible)

|**FASE 1**|<p>**Configuración de parámetros del sistema**</p><p>Configuración base antes del primer período</p>|
| :-: | :- |

**1.1. Parámetros generales**

Se configuran una única vez y se actualizan cuando cambia la normativa o la estructura organizacional:

|**Parámetro**|**Descripción**|
| :- | :- |
|Gestión de Bancos|Registro de entidades bancarias habilitadas: BNB, BISA, Mercantil Santa Cruz, Banco Unión, etc.|
|Rangos salariales|Definición de bandas salariales por categoría y nivel. El rango mínimo debe ser >= SMN Bs 3.300.|
|Niveles salariales|Escalafón de niveles (docente, administrativo, operativo) con sus correspondientes haberes base.|
|Formas de pago|Transferencia bancaria, cheque, caja. Define el canal de dispersión por defecto por empleado.|

**1.2. Tipos de ingreso**

Los tipos de ingreso definen todos los conceptos que suman al Total Ganado del empleado y que son base de cálculo para RC-IVA, Gestora Pública, aguinaldo y prima:

- Haber básico (obligatorio, no puede ser menor al SMN)
- Bono de antigüedad (escalonado según años de servicio: 5%, 11%, 18%, 26%, 34%)
- Horas extra diurnas (recargo del 100% sobre el costo-hora)
- Horas extra nocturnas (recargo del 100% sobre el costo-hora con recargo nocturno del 25%)
- Comisiones por ventas (Retail)
- Bonos por producción o desempeño
- Recargo dominical o feriado (Retail: 100% adicional)
- Viáticos sujetos a rendición (ONG: imputables a proyecto)

**1.3. Tipos de egreso**

Los egresos son todos los conceptos que se descuentan del Total Ganado para obtener el Líquido Pagable:

- Aportes a la Gestora Pública (AFP): 12,71% del Total Ganado — invariante, no modificable
- Retención RC-IVA: 13% sobre el Total Ganado menos el crédito fiscal del Formulario 110
- Descuentos por atrasos y faltas (proporcional al salario diario)
- Anticipos de salario del período corriente
- Cuotas de préstamos institucionales
- Descuentos judiciales (pensión alimenticia, embargos)
- Seguros de salud complementarios (COSSMIL, seguro privado)

**1.4. Parámetros de períodos y gestiones**

Antes de procesar cualquier planilla, se debe configurar el período activo. Un período mal configurado impide el cierre mensual y genera inconsistencias en los acumulados anuales para aguinaldo y prima.

- Definir el mes y año de la gestión activa
- Establecer la fecha de corte para ingresos y egresos variables
- Configurar el calendario de feriados nacionales y departamentales (incluyendo feriados de Santa Cruz: 24 de septiembre)

|**GUARDRAIL LEGAL**|El sistema bloquea cualquier contrato o adenda donde el haber básico sea inferior al SMN vigente (Bs 3.300 para la gestión 2026). Este bloqueo es del motor de cumplimiento del Core HR y no puede ser omitido desde el módulo de Planillas.|
| :- | :- |

|**FASE 2**|<p>**Carga de ingresos del período**</p><p>Todos los conceptos positivos que conforman el Total Ganado</p>|
| :-: | :- |

**2.1. Ingresos manuales**

Para cada período, el operador de planillas o el sistema (vía integración con Control de Asistencia) debe cargar los ingresos variables del mes:

- Horas extra trabajadas por empleado (diurnas y nocturnas)
- Comisiones generadas en el período (Retail)
- Recargos por trabajo en días feriados o domingos
- Bonos puntuales o reconocimientos especiales

La carga manual puede hacerse para un período específico, un grupo de planilla concreto o un empleado individual.

**2.2. Carga de archivos**

El sistema permite importar archivos estructurados (CSV, TXT con formato predefinido) para la carga masiva de ingresos. Esto es especialmente útil para organizaciones con muchos empleados o cuando los datos provienen de sistemas externos (control de asistencia biométrico, sistema de ventas para comisiones).

- Por tipo de planilla (mensual, jornaleros, docentes)
- Por grupo de planilla (una sucursal, un departamento)
- Para una planilla específica dentro del grupo

**2.3. Ingresos periódicos**

Son ingresos que se generan automáticamente cada período sin necesidad de carga manual, porque responden a reglas definidas en los parámetros:

- Bono de antigüedad: el sistema calcula automáticamente el porcentaje correspondiente según los años de servicio del empleado, tomando como base el SMN (ONGs) o 3 SMN (empresas privadas). Base de cálculo inviolable.
- Quinquenio: cuando el empleado cumple 60 meses de antigüedad ininterrumpida, se activa el evento QUINQUENIO\_ELIGIBILITY\_REACHED. El cálculo se basa en el promedio del Total Ganado de los últimos 3 meses (90 días).

|**QUINQUENIO**|El plazo legal para el pago del quinquenio tras la solicitud es de 30 días calendario. Al día 31 sin registro de pago, el sistema aplica automáticamente una multa del 30% sobre el monto total. Esta multa no puede ser revertida una vez disparada.|
| :- | :- |

|**FASE 3**|<p>**Carga de egresos y descuentos**</p><p>Conceptos que reducen el Total Ganado para obtener el Líquido Pagable</p>|
| :-: | :- |

**3.1. Egresos manuales**

Los egresos manuales se registran por período y pueden corresponder a:

- Descuentos por atrasos: se calcula el valor del día de trabajo (haber básico / 30) multiplicado por los días o fracciones de atraso.
- Descuentos por ausencias injustificadas: el sistema consulta el módulo de Asistencia y Permisos (BC: Accruals & Time-Off).
- Multas o penalidades definidas en el reglamento interno del tenant.

**3.2. Anticipos**

Un anticipo es un pago parcial del salario del período corriente, realizado antes del cierre de la planilla. El sistema registra el anticipo y lo descuenta automáticamente al generar la planilla mensual. Los anticipos pueden cargarse:

- Para un tipo de planilla determinado
- Para un grupo de planillas
- Para un empleado individual

**3.3. Descuentos periódicos**

Son descuentos que se aplican automáticamente cada período, similar a los ingresos periódicos. Se configuran una vez y el sistema los aplica hasta que se cumple la condición de término:

- Cuotas de préstamos institucionales: se define el monto de cuota y el número de cuotas. El sistema descuenta automáticamente hasta completar la deuda.
- Seguros complementarios de salud con prima fija mensual.
- Pensión alimenticia por orden judicial: monto o porcentaje fijo mensual.

|**FASE 4**|<p>**RC-IVA — Régimen Complementario al IVA**</p><p>Procesamiento tributario obligatorio según la Ley de Transparencia Fiscal de Bolivia</p>|
| :-: | :- |

El RC-IVA es el impuesto boliviano que grava los ingresos de los dependientes. Su procesamiento en el sistema tiene dos componentes:

**4.1. Carga del Formulario 110 (crédito fiscal)**

Cada empleado puede presentar facturas de compras personales para generar un crédito fiscal que reduzca su obligación tributaria. El sistema permite la carga de estos datos de dos formas:

- Carga manual: el operador de RRHH ingresa el monto del crédito fiscal declarado por el empleado para el período.
- Carga de archivo: importación masiva del reporte generado por el SIAT en Línea (Servicio de Impuestos Nacionales) con los datos de todos los empleados.
- Integración vía API con el SIAT: el sistema puede consultar directamente el portal de Impuestos Nacionales para obtener los saldos de crédito fiscal de cada empleado (funcionalidad avanzada).

**4.2. Procesamiento de saldos RC-IVA**

El cálculo del RC-IVA se realiza sobre el Total Ganado del empleado aplicando la tasa del 13%, y luego restando el crédito fiscal acumulado presentado en el Formulario 110:

|**Concepto**|**Valor / Fórmula**|
| :- | :- |
|Total Ganado|Suma de todos los ingresos del período|
|RC-IVA base|Total Ganado × 13%|
|Crédito fiscal (F-110)|Facturas presentadas × 13%|
|RC-IVA a retener|MAX(0, RC-IVA base – Crédito fiscal)|
|Retención Gestora Pública|Total Ganado × 12,71% (invariante)|

|**RETENCIÓN EXACTA**|La retención para la Gestora Pública (AFP) está fijada estrictamente en 12,71% sobre el Total Ganado. Este porcentaje es un invariante del sistema: no puede ser modificado por configuración, rol ni perfil de tenant. Cualquier intento de cambio debe rechazarse a nivel de motor de cumplimiento.|
| :- | :- |

|**FASE 5**|<p>**Generación de planillas**</p><p>Motor de cálculo y tipos de planilla soportados</p>|
| :-: | :- |

El motor de generación de planillas toma todos los ingresos y egresos del período, aplica las reglas de RC-IVA y Gestora, y produce el documento de planilla con el detalle de Líquido Pagable por empleado.

**5.1. Planilla mensual**

Es la planilla estándar para empleados con contrato a plazo indefinido o plazo fijo activo. Se genera una vez por período y agrupa a los empleados según el tipo y grupo de planilla configurados en los parámetros. El proceso de creación es:

- Seleccionar el período activo
- Seleccionar el grupo de planilla (por sucursal, departamento o categoría)
- El sistema toma el haber básico del contrato y agrega todos los ingresos y egresos del período
- Se calculan las retenciones de RC-IVA y Gestora
- Se genera el borrador de planilla para revisión

**5.2. Planilla de jornaleros y temporales**

Para empleados con contrato temporal o trabajo por días. El cálculo se basa en el costo-hora o costo-día definido en los parámetros. El sistema proporciona el Total Ganado proporcional a los días trabajados en el período.

**5.3. Planilla de reintegro (retroactivo)**

Se genera cuando existe un ajuste salarial con fecha efectiva anterior al período corriente. El sistema calcula la diferencia entre el sueldo pagado y el sueldo que debió pagarse, y genera una planilla de reintegro por el monto de la diferencia. Esto es frecuente en incrementos salariales con fecha retroactiva o en correcciones de errores de períodos anteriores.

**5.4. Planilla de aguinaldo**

El aguinaldo boliviano equivale a un sueldo mensual y debe pagarse antes del 25 de diciembre de cada año. Para empleados que no completaron el año de trabajo, se calcula por duodécimas (un doceavo por cada mes trabajado). La base de cálculo es el promedio del Total Ganado de los tres últimos meses (90 días). Los pasos son:

- El sistema acumula el Total Ganado de los meses de octubre, noviembre y diciembre
- Calcula el promedio de los 90 días
- Para empleados con menos de 12 meses: multiplica el promedio por los meses completos trabajados y divide entre 12
- Genera la planilla de aguinaldo separada de la planilla mensual de diciembre

|**PLAZO AGUINALDO**|El aguinaldo debe pagarse antes del 25 de diciembre. El incumplimiento genera doble aguinaldo según normativa boliviana. El sistema alerta al área de Finanzas con suficiente anticipación para la provisión de fondos.|
| :- | :- |

**5.5. Planilla de prima anual**

La prima equivale a un salario mensual y corresponde a los empleados que hayan trabajado más de tres meses en la empresa durante la gestión. Se paga antes del 31 de marzo del año siguiente. La base de cálculo es el promedio del Total Ganado de los últimos 3 meses del año. El sistema gestiona las duodécimas para empleados con menos de doce meses de servicio.

|**FASE 6**|<p>**Modificación, revisión y aprobación**</p><p>Control de calidad y segregación de funciones</p>|
| :-: | :- |

**6.1. Modificaciones manuales**

Después de la generación del borrador de planilla, el operador autorizado puede realizar ajustes puntuales antes de la revisión formal. El sistema registra cada modificación en el AuditLog con el usuario, la fecha y los valores anteriores y posteriores al cambio (diff histórico).

- Modificación de ingresos de un empleado específico: corrección de horas extra, ajuste de comisión.
- Modificación de egresos: corrección de un descuento mal calculado.
- Modificación de planillas ya generadas: permite reabrir una planilla en estado borrador para correcciones antes de la revisión formal.

**6.2. Revisión**

La persona con el rol de Revisor de Planillas accede al módulo de revisión y verifica la integridad de todos los componentes de la planilla: totales por empleado, consistencia de retenciones, validación de que ningún líquido pagable sea negativo y que todos los empleados activos estén incluidos. La revisión es un paso obligatorio antes de la aprobación.

**6.3. Aprobación con Segregación de Funciones (SoD)**

El sistema implementa Segregación de Funciones (SoD): quien genera la planilla no puede ser la misma persona que la aprueba. La aprobación requiere un usuario con el rol de Aprobador de Planillas, que es diferente al rol de Operador. Una vez aprobada, la planilla queda en estado Aprobado y no puede ser modificada sin un proceso de reapertura con evidencia de auditoría.

|**SoD OBLIGATORIO**|Ninguna planilla con impacto financiero puede ser aprobada por quien la generó. El sistema rechaza automáticamente cualquier intento de auto-aprobación. Esta regla aplica también a modificaciones post-generación: el modificador no puede ser el aprobador.|
| :- | :- |

|**FASE 7**|<p>**Cierre y dispersión bancaria**</p><p>Registro inalterable y generación del archivo de pago</p>|
| :-: | :- |

**7.1. Cierre de planilla mensual**

El cierre es la acción que convierte la planilla aprobada en un registro inalterable del AuditLog. Una vez ejecutado el cierre, los datos de la planilla quedan protegidos como evidencia legal. El cierre genera automáticamente:

- El registro histórico de la planilla en el AuditLog con hash de integridad.
- Los asientos contables para el sistema de contabilidad (imputación analítica por centro de costo o proyecto).
- El evento ACCRUAL\_BALANCE\_DEDUCTED para actualizar los saldos de vacaciones y antigüedad.
- La actualización de los acumulados anuales para el cálculo de aguinaldo y prima.

**7.2. Generación del archivo de dispersión bancaria**

El archivo de transferencia bancaria contiene el detalle de los importes a acreditar en las cuentas de cada empleado. El sistema solo genera este archivo si se cumplen las siguientes condiciones:

- La planilla tiene estado Aprobado y Cerrado.
- Todos los empleados incluidos tienen el evento BANK\_ACCOUNT\_SYNCED registrado (cuenta bancaria validada con documentación de respaldo).
- El presupuesto del centro de costo o proyecto tiene saldo suficiente para cubrir el total de la planilla (validación del BC Financial & Social Compliance).

El archivo se genera en el formato requerido por cada banco (BNB, BISA, Mercantil Santa Cruz, Banco Unión, BCP, etc.) y contiene: número de cuenta, nombre del beneficiario, CI, monto a acreditar y concepto.

|**BLOQUEO BANCARIO**|Si algún empleado de la planilla no tiene el evento BANK\_ACCOUNT\_SYNCED registrado, el sistema bloquea la generación del archivo completo. No es posible generar un archivo parcial. El operador debe resolver la validación bancaria del empleado antes de continuar.|
| :- | :- |

|**FASE 8**|<p>**Consultas y reportes oficiales**</p><p>Reportería para uso interno y presentación ante organismos externos</p>|
| :-: | :- |

El módulo de Planillas genera los siguientes reportes oficiales que pueden ser requeridos por el Ministerio de Trabajo, el Servicio de Impuestos Nacionales (SIN) o la Gestora Pública de la Seguridad Social de Largo Plazo:

**8.1. Reporte de Planilla Oficial**

Planilla en el formato oficial exigido por el Ministerio de Trabajo de Bolivia. Incluye: nombre completo del empleado, CI, cargo, haber básico, bonos, total ganado, descuentos legales y líquido pagable. Este reporte es el documento de referencia ante inspecciones laborales.

**8.2. Reporte de RC-IVA**

Detalle de la retención del RC-IVA por empleado. Incluye: total ganado, crédito fiscal presentado, RC-IVA calculado y RC-IVA retenido. Se utiliza para la presentación de la Declaración Jurada ante el SIN y para la entrega a cada empleado como comprobante de retención.

**8.3. Reporte de Planilla de Aguinaldo**

Reporte oficial del aguinaldo de la gestión. Incluye el desglose de los meses base, el promedio calculado, las duodécimas cuando aplica y el monto final a pagar. Debe conservarse como evidencia legal de cumplimiento del beneficio social.

**8.4. Reporte de Planillas Retroactivas**

Detalle de todos los reintegros procesados en la gestión. Incluye el período original al que corresponde el reintegro, la diferencia calculada y el período en que fue procesado. Este reporte es útil para auditorías internas y para el cálculo del impacto retroactivo en RC-IVA.

**8.5. Reporte de Planillas Manuales**

Registro de todas las modificaciones manuales realizadas sobre planillas generadas automáticamente. Incluye: usuario que realizó la modificación, valor anterior, valor posterior y justificación. Es parte del AuditLog y no puede ser eliminado ni modificado.


# **3. Resumen de invariantes legales del módulo**
Los siguientes invariantes son restricciones del sistema que no pueden ser modificadas por configuración, por ningún rol de usuario ni por el administrador del tenant. Son el reflejo digital de la normativa laboral boliviana:

|**Invariante**|**Regla**|
| :- | :- |
|Piso salarial legal|Haber básico >= SMN vigente (Bs 3.300 en 2026). Bloqueo en contratos y adendas.|
|Retención Gestora Pública|Exactamente el 12,71% del Total Ganado. No configurable.|
|RC-IVA|13% del Total Ganado menos crédito fiscal F-110. No configurable.|
|Quinquenio — plazo de pago|30 días calendario desde la solicitud. Multa automática del 30% al día 31.|
|Aguinaldo — plazo|Antes del 25 de diciembre. Doble aguinaldo por incumplimiento.|
|Prima anual — plazo|Antes del 31 de marzo del año siguiente.|
|Finiquito — plazo|15 días calendario desde el cese. Alerta crítica al día 16.|
|SoD en aprobaciones|El generador de planilla no puede ser el aprobador.|
|AuditLog|Los registros de planillas cerradas son inalterables. Solo se acepta nota de rectificación.|
|Dispersión bancaria|Bloqueada si algún empleado no tiene BANK\_ACCOUNT\_SYNCED.|
|Base de cálculo quinquenio|Promedio del Total Ganado de los últimos 90 días. Incluye todas las variables del trimestre.|
|Onboarding completo|Sin ONBOARDING\_COMPLETED, el empleado no aparece en planillas.|

# **4. Glosario de términos**

|**Término**|**Definición**|
| :- | :- |
|Total Ganado|Suma de todos los ingresos del período: haber básico + bonos + horas extra + comisiones + recargos.|
|Líquido Pagable|Total Ganado menos todas las retenciones y descuentos (RC-IVA, Gestora, anticipos, etc.).|
|SMN|Salario Mínimo Nacional. Bs 3.300 para la gestión 2026.|
|RC-IVA|Régimen Complementario al IVA. Impuesto del 13% sobre ingresos de dependientes.|
|Formulario 110|Declaración jurada del empleado ante el SIN para presentar crédito fiscal y reducir su RC-IVA.|
|Gestora Pública|Entidad estatal boliviana que administra las pensiones de vejez (antes AFP). Retención del 12,71%.|
|Quinquenio|Beneficio social boliviano equivalente a la indemnización acumulada cada 5 años de trabajo continuo.|
|Duodécimas|Cálculo proporcional de aguinaldo o prima para empleados con menos de 12 meses en la gestión.|
|SoD|Segregación of Duties (Segregación de Funciones). Principio de control interno que impide que una sola persona controle un proceso completo.|
|ONBOARDING\_COMPLETED|Evento del Core HR que certifica que un empleado está listo para aparecer en planillas.|
|BANK\_ACCOUNT\_SYNCED|Evento del Core HR que valida la cuenta bancaria del empleado para la dispersión de fondos.|
|AuditLog|Registro histórico inalterable de todos los cambios en el sistema. Base de evidencia legal.|
|Tenant|Instancia del sistema correspondiente a una organización específica (ONG, Retail, Corporativo, Educación).|

5. # **Estructura del Módulo**
El módulo de Payroll es el componente del sistema PeopleCoreIA encargado del cálculo, generación, aprobación, cierre y dispersión de los haberes de todos los colaboradores de la organización, en cumplimiento estricto de la legislación laboral boliviana vigente.

El módulo no opera de forma aislada. Consume datos validados y cerrados desde otros módulos del sistema y nunca recalcula ni almacena reglas de negocio que pertenecen a módulos externos.

**Principio rector:** Payroll no inventa reglas. Solo calcula, genera y paga con base en datos que otros módulos ya validaron.

6. # **Dominios del Módulo**
El módulo de Payroll está dividido en 6 dominios internos. Cada dominio tiene una responsabilidad única y delimitada.

**Dominio 1: Parámetros Operativos**

**Por qué existe:** Payroll necesita una capa de configuración propia que no dependa de Core para operar. Aquí vive todo lo que es específico del proceso de pago: qué bancos están habilitados, cómo está organizado el calendario de períodos, qué formas de pago existen y cómo se agrupan las planillas. Estos parámetros no son reglas legales (esas están en Core) sino decisiones operativas del área de RRHH.

**Aggregates:**

- **PayrollPeriodConfig** (aggregate raíz) 
  - Entidades: PayrollPeriod (mes, año, fecha de corte, estado: Abierto / Cerrado), HolidayCalendarRef (referencia al calendario de feriados de Core BC-1)
  - Value Objects: PeriodStatus, CutoffDate, FiscalYear
- **PayrollGroupConfig** (aggregate raíz) 
  - Entidades: PayrollGroup (agrupación de empleados por sucursal, departamento o categoría), PayrollType (Mensual, Jornaleros, Docentes)
  - Value Objects: GroupCode, PayrollTypeCode
- **BankEntityConfig** (aggregate raíz) 
  - Entidades: BankEntity (BNB, BISA, Mercantil Santa Cruz, Banco Unión, BCP, etc.), BankFileFormat (formato de archivo requerido por cada banco)
  - Value Objects: BankCode, FileFormatSpec
- **PaymentMethodConfig** (aggregate raíz) 
  - Entidades: PaymentMethod (transferencia bancaria, cheque, caja)
  - Value Objects: PaymentChannel, DefaultPaymentMethod

**Dominio 2: Ingresos y Egresos**

**Por qué existe:** Antes de que el motor de planillas pueda calcular cualquier cosa, necesita conocer todos los conceptos positivos y negativos del período. Este dominio es el repositorio de todos los movimientos del período: horas extra aprobadas, comisiones, anticipos, descuentos por ausencias, cuotas de préstamos. Sin este dominio completo y cerrado, no hay planilla posible.

**Aggregates:**

- **IncomeRecord** (aggregate raíz) 
  - Entidades: ManualIncomeEntry (carga individual por operador), FileImportEntry (carga masiva por archivo CSV/TXT), PeriodicIncomeEntry (ingresos automáticos recurrentes: bono de antigüedad, quinquenio)
  - Value Objects: IncomeType (HorasExtra, Comisión, RecargoDominical, BonoAntigüedad, Quinquenio, ViáticoProyecto), IncomeAmount, OvertimeRate, AntiguedadPercentage, PeriodRef
- **DeductionRecord** (aggregate raíz) 
  - Entidades: ManualDeductionEntry (descuentos por atrasos, faltas, multas), AdvancePayment (anticipo del período corriente), PeriodicDeductionEntry (cuotas de préstamos, seguros, pensión alimenticia)
  - Value Objects: DeductionType (Atraso, Ausencia, Anticipo, CuotaPréstamo, PensionAlimenticia, SeguroComplementario), DeductionAmount, LoanInstallment, JudicialOrderRef

**Reglas críticas de este dominio:**

- Un anticipo solo puede cargarse para el período activo, nunca para períodos cerrados.
- Los ingresos periódicos (bono de antigüedad) se calculan automáticamente al abrir el período, usando los datos de antigüedad provistos por Core BC-1.
- El quinquenio se activa únicamente cuando Payroll recibe el evento QUINQUENIO\_ELIGIBILITY\_REACHED desde Core BC-1. La base de cálculo es el promedio del Total Ganado de los últimos 90 días. El plazo de pago es 30 días calendario; al día 31 sin pago registrado, el sistema aplica automáticamente una multa del 30% irreversible.

**Dominio 3: Motor de Planillas**

**Por qué existe:** Es el núcleo del módulo. Toma todos los ingresos y egresos del período ya cargados, consulta las retenciones legales desde Core BC-5 (RC-IVA y Gestora Pública), y produce el documento de planilla con el Líquido Pagable por empleado. Soporta múltiples tipos de planilla porque cada una tiene una base de cálculo y un calendario legal distinto.

**Aggregates:**

- **PayrollRun** (aggregate raíz) 
  - Entidades: PayrollLine (línea de cálculo por empleado: Total Ganado, retenciones, Líquido Pagable), PayrollAdjustment (modificación manual post-generación con diff histórico)
  - Value Objects: PayrollRunType (Mensual, Jornaleros, Reintegro, Aguinaldo, Prima), PayrollStatus (Borrador → Revisado → Aprobado → Cerrado), TotalEarned, NetPayable, RCIVARetained, GestoraRetention, PeriodRef

**Tipos de planilla soportados:**

- **Mensual:** planilla estándar para empleados con contrato activo. Base: haber básico del contrato + todos los ingresos y egresos del período.
- **Jornaleros/Temporales:** base proporcional al costo-hora o costo-día definido en parámetros, multiplicado por los días trabajados validados por TM.
- **Reintegro (Retroactivo):** se genera cuando existe un ajuste salarial con fecha efectiva anterior al período corriente. El motor calcula la diferencia entre lo pagado y lo que debió pagarse.
- **Aguinaldo:** equivale a un sueldo mensual. Base de cálculo: promedio del Total Ganado de octubre, noviembre y diciembre (90 días). Para empleados con menos de 12 meses: duodécimas proporcionales. Plazo legal: antes del 25 de diciembre.
- **Prima Anual:** equivale a un salario mensual para empleados con más de 3 meses en la gestión. Base de cálculo: promedio del Total Ganado de los últimos 3 meses del año. Plazo legal: antes del 31 de marzo del año siguiente.

**Dominio 4: Aprobación y Control**

**Por qué existe:** Ninguna planilla con impacto financiero puede avanzar sin un control formal de calidad y sin Segregación de Funciones. Este dominio implementa el principio SoD: quien genera no aprueba, y cada acción queda registrada de forma inalterable. Es la barrera entre el borrador de planilla y el dinero real.

**Aggregates:**

- **PayrollApproval** (aggregate raíz) 
  - Entidades: ReviewRecord (registro de revisión formal con usuario y timestamp), ApprovalRecord (registro de aprobación con usuario aprobador distinto al generador)
  - Value Objects: ApprovalStatus (PendienteRevisión → Revisado → Aprobado → Rechazado), ReviewerRef, ApproverRef, SoDViolationFlag
- **AuditLog** (aggregate raíz) 
  - Entidades: AuditEntry (registro inalterable de cada acción: usuario, timestamp, valor anterior, valor posterior, justificación)
  - Value Objects: AuditAction, DiffRecord, ImmutabilityHash

**Reglas críticas de este dominio:**

- El sistema rechaza automáticamente cualquier intento de auto-aprobación. El ApproverRef no puede ser igual al generador de la planilla ni al último modificador.
- Una planilla en estado Aprobado no puede ser modificada sin un proceso formal de reapertura que genera evidencia en el AuditLog.
- Los registros del AuditLog son inalterables. Solo se acepta nota de rectificación adjunta; el registro original nunca se elimina ni modifica.

**Dominio 5: Dispersión y Cierre**

**Por qué existe:** Una vez aprobada la planilla, el dinero debe llegar a los empleados. Este dominio gestiona la generación del archivo bancario y el cierre definitivo del período. El cierre convierte la planilla en un registro legal inalterable y dispara los eventos hacia otros módulos que dependen de los acumulados de Payroll.

**Aggregates:**

- **BankDispersionFile** (aggregate raíz) 
  - Entidades: BankTransferLine (línea por empleado: número de cuenta, nombre, CI, monto, concepto), BankFileExport (archivo generado en formato específico por banco)
  - Value Objects: BankFileStatus (Pendiente → Generado → Enviado), TransferAmount, BankAccountRef, BankFileFormat
- **PayrollClosure** (aggregate raíz) 
  - Entidades: ClosureRecord (registro del cierre con hash de integridad), AccountingEntry (asiento contable por centro de costo o proyecto)
  - Value Objects: ClosureStatus, IntegrityHash, CostCenterRef, ProjectRef, AccrualDeductionRef

**Reglas críticas de este dominio:**

- El archivo de dispersión bancaria solo se genera si la planilla tiene estado Aprobado y Cerrado, y si el 100% de los empleados incluidos tienen el evento BANK\_ACCOUNT\_SYNCED registrado. Si un solo empleado no lo tiene, el archivo completo queda bloqueado. No existe generación parcial.
- La validación de presupuesto se consulta a Core BC-5 antes de generar el archivo: el centro de costo o proyecto debe tener saldo suficiente para cubrir el total de la planilla.
- Una vez ejecutado el cierre, la planilla queda protegida con hash de integridad. No puede modificarse.

**Dominio 6: Reportería**

**Por qué existe:** El módulo debe producir documentos oficiales que pueden ser requeridos por el Ministerio de Trabajo, el Servicio de Impuestos Nacionales (SIN) y la Gestora Pública. Estos reportes son evidencia legal y no pueden generarse desde planillas que no estén cerradas.

**Aggregates:**

- **PayrollReport** (aggregate raíz) 
  - Entidades: ReportExport (documento generado con timestamp y usuario solicitante)
  - Value Objects: ReportType (PlanillaOficial, RCIVA, Aguinaldo, Retroactivo, Manual), ReportStatus (Generado → Descargado), PeriodRef

**Tipos de reporte:**

- **Planilla Oficial:** formato exigido por el Ministerio de Trabajo. Incluye nombre, CI, cargo, haber básico, bonos, total ganado, descuentos y líquido pagable.
- **RC-IVA:** detalle de retención por empleado. Total ganado, crédito fiscal, RC-IVA calculado y retenido. Para presentación ante el SIN.
- **Aguinaldo:** desglose de meses base, promedio calculado, duodécimas y monto final. Evidencia legal de cumplimiento del beneficio social.
- **Retroactivos:** detalle de reintegros procesados en la gestión, período original, diferencia calculada y período de procesamiento.
- **Modificaciones Manuales:** registro de todas las intervenciones manuales sobre planillas generadas automáticamente. Parte del AuditLog, no puede eliminarse.

# **7. Comunicación con Módulos Externos**
**Módulo Core — BC-1: Workforce & Organization Master**

Payroll **consulta** (sincrónico) los siguientes datos antes de procesar cualquier planilla:

|**Dato consultado**|**Para qué lo usa Payroll**|
| :- | :- |
|Relationship.status == ACTIVE|Verificar que el empleado está habilitado para aparecer en planilla|
|Contract.basic\_salary|Base del haber básico de cada línea de planilla|
|Person.bank\_account|Referencia de cuenta bancaria para dispersión|
|SalaryRange / SalaryLevel|Validar que el haber básico esté dentro del rango del nivel del empleado|
|HolidayCalendar|Clasificar horas festivas recibidas desde TM|

Payroll **consume eventos** (asíncrono) desde Core BC-1:

|**Evento**|**Qué hace Payroll con él**|
| :- | :- |
|ONBOARDING\_COMPLETED|Habilita al empleado en el motor de planillas. Sin este evento, el empleado no aparece en ninguna planilla|
|CONTRACT\_ACTIVATED|Confirma que el contrato superó validaciones legales. Actualiza el haber básico de referencia en PayrollLine|
|BANK\_ACCOUNT\_SYNCED|Desbloquea la generación del archivo de dispersión para ese empleado|
|QUINQUENIO\_ELIGIBILITY\_REACHED|Activa el cálculo del quinquenio en el dominio de Ingresos y Egresos|
|EMPLOYEE\_DEACTIVATED|Bloquea al empleado en el período activo y lo excluye de planillas futuras|

**Módulo Core — BC-5: Financial & Social Compliance**

Payroll **consulta** (sincrónico):

|**Dato consultado**|**Para qué lo usa Payroll**|
| :- | :- |
|Porcentaje RC-IVA (13%)|Cálculo de retención en cada PayrollLine|
|Porcentaje Gestora Pública (12,71%)|Cálculo de retención en cada PayrollLine. Invariante, no configurable|
|BudgetBalance por CostCenter / Project|Validación previa a la generación del archivo de dispersión bancaria|

**Nota:** Payroll no almacena ni recalcula estos porcentajes. Los consulta en tiempo real desde BC-5. Si BC-5 actualiza un porcentaje por cambio normativo, Payroll lo aplica automáticamente en el siguiente cálculo sin necesidad de reconfiguración.

**Módulo Time & Marcaciones (TM)**

Payroll **consume eventos** (asíncrono):

|**Evento**|**Payload**|**Qué hace Payroll con él**|
| :- | :- | :- |
|ATTENDANCE\_PERIOD\_CLOSED|TimesheetPayrollHandoff (regular\_hours, overtime\_hours, night\_hours, holiday\_hours por empleado)|Alimenta el dominio de Ingresos con las horas validadas del período. Sin este evento, el período de Payroll no puede iniciarse|

**Regla crítica:** Payroll no usa datos de Scheduling directamente. La fuente de verdad de horas trabajadas es siempre TM. Si TM no ha cerrado el período, Payroll no puede generar ninguna planilla del mismo período.

**Payroll emite eventos hacia otros módulos:**

|**Evento**|**Destino**|**Trigger**|
| :- | :- | :- |
|PAYROLL\_PERIOD\_CLOSED|Core BC-4 (Accruals & Seniority)|Al ejecutar el cierre del período. Actualiza saldos de vacaciones y antigüedad|
|PAYROLL\_ACCOUNTING\_ENTRIES\_READY|Sistema de Contabilidad|Al cierre, genera asientos contables con imputación analítica por centro de costo o proyecto|
|DISPERSION\_FILE\_GENERATED|Core BC-5 / Finanzas|Al generar el archivo bancario aprobado y listo para envío|

# **8. Workflow del Módulo de Payroll**
El workflow de Payroll tiene un flujo principal (Planilla Mensual) con bifurcaciones para tipos de planilla especiales. A continuación se detalla cada paso con sus actores, validaciones, eventos y condiciones de falla.

**Paso 0 — Prerequisito: Habilitación del Empleado**

**Actor:** Sistema (automático) **Trigger:** Evento ONBOARDING\_COMPLETED recibido desde Core BC-1

El sistema verifica que el empleado cumple los cuatro pilares antes de habilitarlo en el motor de planillas:

1. Identidad verificada (Person con CI registrado en Core BC-1)
1. Vínculo laboral activo (Relationship.status == ACTIVE en Core BC-1)
1. Contrato aprobado con SoD y haber básico >= SMN Bs 3.300 (Contract en Core BC-1)
1. Posición asignada con plaza presupuestada (Position con headcount disponible en Core BC-1)

**Si todo está correcto:** el empleado queda registrado como elegible en el motor de planillas de Payroll. **Si falta alguno de los cuatro pilares:** el sistema registra el intento y no habilita al empleado. No genera error visible al operador de Payroll — la responsabilidad de completar el onboarding es de Core.

**Evento consumido:** ONBOARDING\_COMPLETED **Evento emitido:** ninguno. Acción interna de habilitación.

**Paso 1 — Configuración del Período Activo**

**Actor:** Operador de Planillas **Trigger:** Manual. El operador abre el período mensual desde el panel de Parámetros Operativos.

El operador configura:

- Mes y año de la gestión activa
- Fecha de corte para ingresos y egresos variables
- Confirmación del calendario de feriados nacionales y departamentales (referencia desde Core BC-1)
- Agrupación de planillas activas para el período (por sucursal, departamento o categoría)

**Validación del sistema:**

- El período no puede abrirse si existe un período anterior en estado Abierto sin cerrar.
- El calendario de feriados debe estar publicado en Core BC-1 para el mes en curso. Si no está disponible, el sistema alerta al operador pero no bloquea la apertura.

**Si todo está correcto:** el PayrollPeriod queda en estado Abierto y el sistema está listo para recibir ingresos y egresos del período. **Si hay un período anterior sin cerrar:** el sistema bloquea la apertura y muestra el período bloqueante.

**Evento emitido:** PAYROLL\_PERIOD\_OPENED (interno, notifica a los dominios de Ingresos y Egresos que pueden comenzar a recibir movimientos)

**Paso 2 — Recepción del Cierre de Marcaciones**

**Actor:** Sistema (automático) **Trigger:** Evento ATTENDANCE\_PERIOD\_CLOSED recibido desde el módulo TM con el payload TimesheetPayrollHandoff

El sistema recibe el resumen validado de horas por empleado:

- Horas regulares
- Horas extra diurnas
- Horas nocturnas (con recargo del 25% según LGT Bolivia)
- Horas en días feriados (con recargo del 100%)
- Horas en días domingo — Retail (con recargo del 100%)

El sistema verifica que el período del handoff coincide con el período activo abierto en el Paso 1. Si coincide, los datos se cargan automáticamente en el dominio de Ingresos como IncomeRecord de tipo automático, vinculados a cada empleado.

**Si el período de TM no coincide con el período activo de Payroll:** el evento queda en cola de espera hasta que el operador abra el período correcto. **Si TM no ha cerrado el período:** Payroll no puede avanzar al Paso 3. El sistema muestra el bloqueo al operador con el estado actual de TM.

**Evento consumido:** ATTENDANCE\_PERIOD\_CLOSED **Evento emitido:** ninguno. Carga interna de datos.

**Paso 3 — Carga de Ingresos del Período**

**Actor:** Operador de Planillas / Sistema (automático para ingresos periódicos) **Trigger:** Período activo abierto + datos de TM recibidos

El operador completa los ingresos variables que TM no cubre:

**3.1 Ingresos manuales:**

- Comisiones por ventas (Retail) del período
- Bonos por producción o desempeño
- Viáticos sujetos a rendición (ONG, imputables a proyecto)
- Reconocimientos o bonos puntuales

**3.2 Carga por archivo:** Para organizaciones con muchos empleados, el sistema permite importar archivos CSV o TXT con formato predefinido. El sistema valida el archivo antes de importar: detecta empleados no habilitados, montos negativos o períodos incorrectos, y rechaza el archivo completo si encuentra errores, mostrando el detalle línea por línea.

**3.3 Ingresos periódicos (automáticos):** El sistema calcula y carga automáticamente al abrir el período:

- **Bono de antigüedad:** el sistema consulta los años de servicio desde Core BC-1 y aplica el porcentaje escalonado (5%, 11%, 18%, 26%, 34%). La base de cálculo es el SMN para ONGs o 3 SMN para empresas privadas. Este porcentaje es inviolable.
- **Quinquenio:** solo si se recibió el evento QUINQUENIO\_ELIGIBILITY\_REACHED desde Core BC-1. El sistema calcula el promedio del Total Ganado de los últimos 90 días y genera el ingreso. El plazo de pago desde la solicitud es 30 días calendario. Al día 31 sin registro de pago, el sistema aplica automáticamente una multa del 30% sobre el monto total. Esta multa es irreversible.

**Evento emitido:** ninguno. Todos los ingresos quedan en estado Pendiente hasta el paso de generación.

**Paso 4 — Carga de Egresos y Descuentos**

**Actor:** Operador de Planillas / Sistema (automático para descuentos periódicos) **Trigger:** Período activo abierto

**4.1 Egresos manuales:**

- Descuentos por atrasos: el sistema calcula el valor del día de trabajo (haber básico / 30) multiplicado por los días o fracciones de atraso. Los datos de atraso provienen del WorkedHoursSummary de TM.
- Descuentos por ausencias injustificadas: el sistema consulta las ausencias marcadas como UNJUSTIFIED\_ABSENCE en TM y las convierte en descuentos automáticamente.
- Multas o penalidades definidas en el reglamento interno del tenant.

**4.2 Anticipos:** Un anticipo es un pago parcial del salario del período corriente realizado antes del cierre. El sistema lo registra como DeductionRecord y lo descuenta automáticamente al generar la planilla. Los anticipos pueden cargarse para un empleado individual, un grupo o un tipo de planilla completo.

**4.3 Descuentos periódicos (automáticos):** Se configuran una vez y el sistema los aplica hasta cumplir la condición de término:

- Cuotas de préstamos institucionales: el sistema descuenta automáticamente hasta completar la deuda.
- Seguros complementarios de salud con prima fija mensual (COSSMIL, seguro privado).
- Pensión alimenticia por orden judicial: monto o porcentaje fijo mensual.
- Descuentos judiciales (embargos).

**Evento emitido:** ninguno. Todos los egresos quedan en estado Pendiente hasta el paso de generación.

**Paso 5 — Procesamiento RC-IVA (Formulario 110)**

**Actor:** Operador de Planillas / Sistema **Trigger:** Período activo con ingresos y egresos cargados

El RC-IVA es el impuesto boliviano del 13% sobre los ingresos de dependientes. Payroll no define este porcentaje — lo consulta desde Core BC-5.

**5.1 Carga del crédito fiscal:** Cada empleado puede presentar facturas de compras personales para generar crédito fiscal que reduzca su retención. El operador puede cargar estos datos de tres formas:

- Carga manual por empleado.
- Importación masiva del reporte generado por el SIAT en Línea (Servicio de Impuestos Nacionales).
- Integración vía API con el SIAT para obtener saldos directamente.

**5.2 Cálculo (ejecutado por el motor al generar la planilla en Paso 6):**

|**Concepto**|**Fórmula**|
| :- | :- |
|Total Ganado|Suma de todos los ingresos del período|
|RC-IVA base|Total Ganado × 13% (porcentaje desde Core BC-5)|
|Crédito fiscal (F-110)|Facturas presentadas × 13%|
|RC-IVA a retener|MAX(0, RC-IVA base − Crédito fiscal)|
|Retención Gestora Pública|Total Ganado × 12,71% (invariante desde Core BC-5)|

**Regla crítica:** el 12,71% de la Gestora Pública es un invariante absoluto. No puede ser modificado por configuración, por ningún rol de usuario ni por el administrador del tenant. Payroll lo consulta desde Core BC-5 y lo aplica sin posibilidad de override.

**Paso 6 — Generación de Planilla**

**Actor:** Operador de Planillas **Trigger:** Manual. El operador ejecuta la generación desde el panel del período activo.

El motor toma todos los ingresos y egresos del período, aplica las retenciones de RC-IVA y Gestora consultadas desde Core BC-5, y produce el borrador de planilla.

**El motor valida antes de generar:**

- Todos los empleados habilitados del grupo tienen datos de TM recibidos.
- Ningún Líquido Pagable resulta negativo. Si alguno es negativo, el sistema detiene la generación y muestra el detalle del empleado afectado para corrección manual.
- El haber básico de cada empleado es >= SMN Bs 3.300 (validación desde Core BC-1).

**Bifurcaciones según tipo de planilla:**

**→ Planilla Mensual (flujo estándar):** El motor genera una PayrollLine por empleado con: Total Ganado, RC-IVA retenido, Gestora retenida, otros descuentos y Líquido Pagable. El resultado es un borrador en estado Borrador.

**→ Planilla de Jornaleros/Temporales:** El motor calcula el Total Ganado proporcional a los días trabajados según el WorkedHoursSummary de TM. El resto del flujo es idéntico al mensual.

**→ Planilla de Reintegro (Retroactivo):** El operador selecciona el período original y el período de corrección. El motor calcula la diferencia entre lo pagado y lo que debió pagarse. Genera una planilla separada solo por el monto de la diferencia.

**→ Planilla de Aguinaldo (bifurcación diciembre):** El sistema acumula el Total Ganado de octubre, noviembre y diciembre. Calcula el promedio de los 90 días. Para empleados con menos de 12 meses aplica duodécimas. Genera la planilla de aguinaldo separada de la planilla mensual de diciembre. **Alerta crítica:** el sistema notifica al área de Finanzas con anticipación suficiente para la provisión de fondos. El plazo legal es antes del 25 de diciembre. El incumplimiento genera doble aguinaldo según normativa boliviana.

**→ Planilla de Prima Anual (bifurcación marzo):** Base de cálculo: promedio del Total Ganado de los últimos 3 meses de la gestión anterior. Solo para empleados con más de 3 meses trabajados en la gestión. Duodécimas para empleados con menos de 12 meses. Plazo legal: antes del 31 de marzo.

**Evento emitido:** PAYROLL\_DRAFT\_GENERATED (interno, notifica al dominio de Aprobación y Control que hay una planilla lista para revisión)

**Paso 7 — Modificaciones al Borrador**

**Actor:** Operador de Planillas (autorizado) **Trigger:** Planilla en estado Borrador

Antes de la revisión formal, el operador puede realizar ajustes puntuales sobre el borrador:

- Corrección de horas extra de un empleado específico.
- Ajuste de una comisión mal cargada.
- Corrección de un descuento mal calculado.

**Cada modificación:**

- Queda registrada en el AuditLog con: usuario, timestamp, valor anterior, valor posterior y justificación obligatoria.
- No puede ser eliminada del historial.
- El modificador queda registrado y no podrá ser el aprobador de esa planilla (SoD).

**Evento emitido:** ninguno. Las modificaciones son internas al borrador.

**Paso 8 — Revisión Formal**

**Actor:** Revisor de Planillas (rol diferente al Operador) **Trigger:** Planilla en estado Borrador, notificación desde PAYROLL\_DRAFT\_GENERATED

El Revisor verifica:

- Totales por empleado son matemáticamente correctos.
- Las retenciones de RC-IVA y Gestora son consistentes con los porcentajes vigentes de Core BC-5.
- Ningún Líquido Pagable es negativo.
- Todos los empleados activos del grupo están incluidos.
- No existen empleados deshabilitados incluidos por error.

**Si la revisión es aprobada:** la planilla transiciona a estado Revisado. **Si el Revisor encuentra errores:** devuelve la planilla a estado Borrador con una nota de rechazo en el AuditLog. El operador debe corregir y el ciclo de revisión se reinicia.

**Evento emitido:** PAYROLL\_REVIEWED (interno, notifica al Aprobador que la planilla está lista para aprobación)

**Paso 9 — Aprobación con SoD**

**Actor:** Aprobador de Planillas (rol diferente al Operador y al Revisor) **Trigger:** Planilla en estado Revisado, notificación desde PAYROLL\_REVIEWED

El sistema verifica automáticamente antes de permitir la aprobación:

- El AprobadorRef es diferente al generador de la planilla.
- El AprobadorRef es diferente al último modificador de la planilla.
- El AprobadorRef es diferente al Revisor.

**Si alguna verificación falla:** el sistema rechaza el intento de aprobación con mensaje de violación SoD y registra el intento en el AuditLog. **Si todas las verificaciones pasan:** el Aprobador confirma la planilla. La planilla transiciona a estado Aprobado.

Una planilla en estado Aprobado no puede ser modificada sin un proceso formal de reapertura que genera evidencia de auditoría.

**Evento emitido:** PAYROLL\_APPROVED (notifica al dominio de Dispersión y Cierre que la planilla está lista para el archivo bancario)

**Paso 10 — Generación del Archivo de Dispersión Bancaria**

**Actor:** Operador de Planillas / Sistema **Trigger:** Planilla en estado Aprobado + evento PAYROLL\_APPROVED

El sistema verifica las siguientes condiciones antes de generar el archivo:

1. La planilla tiene estado Aprobado.
1. El 100% de los empleados incluidos tienen el evento BANK\_ACCOUNT\_SYNCED registrado en Core BC-1. Si un solo empleado no lo tiene, el archivo completo queda bloqueado. No existe generación parcial.
1. El presupuesto del centro de costo o proyecto tiene saldo suficiente (consulta sincrónica a Core BC-5).

**Si todas las condiciones se cumplen:** el sistema genera el archivo en el formato específico del banco de cada empleado (BNB, BISA, Mercantil Santa Cruz, Banco Unión, BCP, etc.). El archivo contiene por cada línea: número de cuenta, nombre del beneficiario, CI, monto a acreditar y concepto.

**Si alguna condición falla:**

- Empleado sin BANK\_ACCOUNT\_SYNCED: el sistema muestra el listado de empleados bloqueantes. El operador debe resolver la validación bancaria en Core BC-1 antes de continuar.
- Presupuesto insuficiente: el sistema alerta al área de Finanzas y bloquea la generación hasta que Core BC-5 confirme saldo disponible.

**Evento emitido:** DISPERSION\_FILE\_GENERATED (notifica a Core BC-5 y Finanzas que el archivo está listo para envío al banco)

**Paso 11 — Cierre del Período**

**Actor:** Operador de Planillas (autorizado para cierre) **Trigger:** Archivo de dispersión generado y enviado al banco

El cierre convierte la planilla aprobada en un registro legal inalterable. Una vez ejecutado, no puede revertirse.

**El cierre genera automáticamente:**

1. **Registro histórico inalterable:** la planilla queda protegida con hash de integridad en el AuditLog. Cualquier consulta futura sobre ese período accede a este registro sellado.
1. **Asientos contables:** imputación analítica por centro de costo o proyecto hacia el sistema de contabilidad.
1. **Actualización de acumulados:** los acumulados anuales para aguinaldo y prima quedan actualizados con los valores del período cerrado.

**Evento emitido:**

- PAYROLL\_PERIOD\_CLOSED → hacia Core BC-4 (Accruals & Seniority) para actualizar saldos de vacaciones y antigüedad.
- PAYROLL\_ACCOUNTING\_ENTRIES\_READY → hacia el sistema de contabilidad con los asientos del período.

**Paso 12 — Generación de Reportes Oficiales**

**Actor:** Operador de Planillas / Analista de RRHH **Trigger:** Planilla en estado Cerrado

Los reportes solo pueden generarse desde planillas cerradas. No existe reporte oficial desde borradores o planillas en revisión.

**Reportes disponibles:**

- **Planilla Oficial** (Ministerio de Trabajo): nombre, CI, cargo, haber básico, bonos, total ganado, descuentos legales, líquido pagable.
- **RC-IVA** (SIN): total ganado, crédito fiscal, RC-IVA calculado y retenido por empleado.
- **Aguinaldo**: desglose de meses base, promedio, duodécimas y monto final.
- **Retroactivos**: período original, diferencia calculada, período de procesamiento.
- **Modificaciones Manuales**: todas las intervenciones sobre planillas automáticas, con usuario, valores y justificación. Parte del AuditLog, inalterable.

# **9. Invariantes Legales del Módulo**
Estas restricciones no pueden ser modificadas por configuración, por ningún rol de usuario ni por el administrador del tenant. Son el reflejo digital de la normativa laboral boliviana.

|**Invariante**|**Regla**|
| :- | :- |
|Piso salarial legal|Haber básico >= SMN vigente (Bs 3.300 en 2026). Validado desde Core BC-1.|
|Retención Gestora Pública|Exactamente el 12,71% del Total Ganado. Consultado desde Core BC-5. No configurable.|
|RC-IVA|13% del Total Ganado menos crédito fiscal F-110. Consultado desde Core BC-5. No configurable.|
|Quinquenio — plazo de pago|30 días calendario desde la solicitud. Multa automática del 30% al día 31. Irreversible.|
|Aguinaldo — plazo|Antes del 25 de diciembre. Doble aguinaldo por incumplimiento.|
|Prima anual — plazo|Antes del 31 de marzo del año siguiente.|
|Finiquito — plazo|15 días calendario desde el cese. Alerta crítica al día 16.|
|SoD en aprobaciones|El generador no puede ser el revisor ni el aprobador. El modificador no puede ser el aprobador.|
|AuditLog|Los registros de planillas cerradas son inalterables. Solo se acepta nota de rectificación adjunta.|
|Dispersión bancaria|Bloqueada si algún empleado no tiene BANK\_ACCOUNT\_SYNCED. Sin generación parcial.|
|Base de cálculo quinquenio|Promedio del Total Ganado de los últimos 90 días. Incluye todas las variables del trimestre.|
|Onboarding completo|Sin ONBOARDING\_COMPLETED, el empleado no aparece en planillas.|
|TM cerrado|Sin ATTENDANCE\_PERIOD\_CLOSED, el período de Payroll no puede generarse.|

