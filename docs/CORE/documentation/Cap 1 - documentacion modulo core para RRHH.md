Integrantes:

Edgar Chavez Rojas

Adrián Flores Arteaga

Jose María Méndez Ramos

Rafaela Ribera Velasquez

Katherine Vargas Eguez

Este documento resume la estructura lógica de la plataforma, diseñada para soportar los perfiles de ONG, Retail, Corporativo y Educación en Bolivia.

I. Mapa de Dominios (Los 16 Pilares con Invariantes)

1. Domain: Organization & Workforce Structure

Propósito: Definir la infraestructura lógica, territorial y de centros de costo.

Entidades Clave: OrgUnit, OrgHierarchy, CostCenter, Campus/Sede.

Lógica Enterprise: Soporta jerarquías múltiples (administrativa, funcional, académica).

Invariantes:

No Unidades Huérfanas: Toda unidad (excepto la raíz) debe pertenecer a una jerarquía; evita empleados sin dependencia clara.

Unicidad de Jefe Administrativo: Un colaborador no puede tener dos jefes administrativos simultáneos para evitar conflictos de aprobación.

2. Domain: Person & Identity

Propósito: Gestionar la identidad civil única del ser humano.

Entidades Clave: Person, PartyIdentifier (CI, Pasaporte), ContactPoint .

Lógica Enterprise: Reglas de deduplicación para evitar registros duplicados de una misma identidad civil.

Invariantes:

Identidad Única: Una persona física solo posee un registro maestro (Person ID), vital para el cálculo de antigüedad en reingresos.

3. Domain: Employment Relationship & Lifecycle

Propósito: Administrar el vínculo legal y operativo (onboarding, movimientos, offboarding).

Entidades Clave: Relationship, WorkerProfile, AcademicProfile .

Lógica Enterprise: Soporta el "Multi-vínculo" (ej. administrativo y docente simultáneo).

Invariantes:

No Traslape de Vínculos Primarios: Impide dos vínculos laborales primarios de tiempo completo activos para evitar contingencias legales por doble percepción.

4. Domain: Position & Headcount Management

Propósito: Controlar las plazas presupuestadas y descriptores de cargo.

Entidades Clave: Job, Position, Grade/Band.

Lógica Enterprise: Vincula cada posición a una fuente de financiamiento, esencial para ONGs.

Invariantes:

Control de Plazas (Headcount): Impide asignar personal a una posición sin plaza vacante autorizada presupuestariamente.

5. Domain: Contracts & Legal Employment Terms

Propósito: Traducir la legislación laboral en reglas digitales y vigencias.

Entidades Clave: Contract, ContractAddendum, EligibilityMatrix.

Lógica Enterprise: Objeto vivo que dispara reglas de jornada y beneficios.

Invariantes:

Integridad Temporal (As-Of): Prohíbe adendas con vigencias superpuestas; los cambios deben ser lineales para cálculos retroactivos.

Estado Aprobado: Un contrato solo se activa si posee evidencia firmada y aprobación mediante flujo de Segregación de Funciones (SoD).

6. Domain: Digital Kardex & Document Compliance

Propósito: Asegurar validez documental y cumplimiento de requisitos industriales.

Entidades Clave: DocumentRecord, ComplianceRequirement, ValidationStatus .

Lógica Enterprise: Motor de estados de cumplimiento (Pendiente, Validado, Expirado).

Invariantes:

Bloqueo por Ilicitud: Cargos críticos (ej. Cajero) no pasan a "Activo" sin validación de documentos obligatorios (antecedentes/garantías).

7. Domain: Assets & Equipment Assignment

Propósito: Rastrear responsabilidad del trabajador sobre bienes de la empresa.

Entidades Clave: AssignedAsset, AssetLog.

Lógica Enterprise: Automatiza el control de activos en procesos de transferencia o baja.

Invariantes:

Certificación de Devolución: El proceso de liquidación no cierra financieramente si existen activos de alto valor marcados como "No Devueltos" sin justificación.

8. Domain: Employee & Manager Self-Service (ESS/MSS)

Propósito: Interfaz descentralizada para autogestión y aprobaciones.

Entidades Clave: SelfServiceAction, ApprovalWorkflow, Notification.

Lógica Enterprise: Acceso móvil nativo con seguridad y cifrado punto a punto.

Invariantes:

Jerarquía de Aprobación: Ninguna solicitud con impacto financiero se auto-aprueba; requiere validación de un nivel superior.

9. Domain: Compliance & Policy Engine

Propósito: Centralizar leyes bolivianas actualizadas (2026).

Entidades Clave: PolicyRule, LegalThreshold, RuleOverride.

Lógica Enterprise: Permite actualizar parámetros legales (ej. SMN Bs 3.300) sin alterar código.

Invariantes:

Piso Salarial Legal: Bloquea contratos con haber básico inferior al SMN vigente (Bs 3.300).

Tope de Jornada por Género: Impide programar más de 40h semanales para mujeres sin clasificarlas como horas extra.

10. Domain: Budget Allocation & Funding Control

Propósito: Imputar costos laborales a proyectos o sucursales.

Entidades Clave: FundingSource, BudgetLine, LaborCostSplit.

Lógica Enterprise: Garantiza que no existan puestos sin respaldo presupuestario.

Invariantes:

Consistencia del 100%: La suma de la distribución porcentual de un sueldo entre proyectos debe ser exactamente el 100%.

11. Domain: Leave, Absences & Permissions (Base)

Propósito: Administrar descansos, vacaciones y días compensatorios.

Entidades Clave: LeaveRequest, AccrualBalance, HolidayCalendar.

Lógica Enterprise: Automatiza feriados nacionales y departamentales (ej. 24 de septiembre en SCZ).

Invariantes:

No Saldo Negativo Forzado: Impide solicitar vacaciones que excedan el saldo acumulado salvo política explícita de anticipo.

12. Domain: Workflow, Audit & Legal Evidence

Propósito: Trazabilidad inalterable y segregación de funciones (SoD).

Entidades Clave: AuditLog, WorkflowInstance, ElectronicEvidence.

Lógica Enterprise: Almacena el "diff" histórico de cambios salariales o contractuales.

Invariantes:

Inalterabilidad de Auditoría: Los registros de auditoría cerrados no pueden ser modificados, garantizando defensa ante juicios laborales.

13. Domain: Seniority, Benefits & Accruals

Propósito: Monitorear beneficios de largo plazo e indexación legal.

Entidades Clave: SeniorityRecord, QuinquenioProvision, BenefitAccrual.

Lógica Enterprise: Detección automática de consolidación del Quinquenio (60 meses).

Invariantes:

Base de Cálculo Inviolable: Bono de antigüedad en ONGs sobre 1 SMN y en Privadas sobre 3 SMN; inalterable por configuración.

14. Domain: Social Security & Regulatory Compliance

Propósito: Administrar afiliaciones y aportes a entes gestores.

Entidades Clave: SocialSecurityAccount, HealthProvider, TaxForm110.

Lógica Enterprise: Implementa la tasa del 13% del "IVA Transparente" para RC-IVA.

Invariantes:

Deducción Laboral Exacta: Retención para la Gestora Pública fijada estrictamente en 12,71% sobre el Total Ganado.

15. Domain: Talent Inventory & Performance Foundations

Propósito: Expediente cualitativo del crecimiento y competencias.

Entidades Clave: PerformanceSnapshot, SkillSet, TrainingHistory.

Lógica Enterprise: Escalafón docente y acreditaciones académicas para el sector educación.

Invariantes:

Vigencia de Certificación: Invalida asignación a puestos críticos si la certificación profesional ha expirado.

16. Domain: AI Insights & Predictive Analytics

Propósito: Predecir riesgos legales y financieros mediante patrones de datos.

Entidades Clave: TrendPrediction, LaborCostForecast, ComplianceAlert.

Lógica Enterprise: Alerta sobre incrementos vegetativos de antigüedad y provisiones de caja.

Invariantes:

Neutralidad Algorítmica: La IA no puede sugerir acciones que violen invariantes legales de otros dominios (ej. sueldos bajo el SMN).











**1. BC: Workforce & Organization Master (El "Núcleo Operativo")**

**Dominios agrupados:** \*                                   							    1. Org Structure Person & Identity

3\.Relationship & Lifecycle

4\.Position & Headcount

- **Por qué esta agrupación:** Estos cuatro dominios comparten la entidad "Persona" y su ubicación en la empresa. Separarlos obligaría a realizar constantes consultas entre módulos para saber quién es el jefe de quién o en qué sucursal está una persona.
- **Explicación técnica:** Aquí se resuelve la **Unicidad de la Persona** y el modelo **Persona ≠ Relación ≠ Asignación** en un solo esquema transaccional, garantizando que el historial (Effective Dating) sea consistente en toda la estructura organizacional.

**2. BC: Employment Terms & Regulatory Compliance (El "Cerebro Legal")**

**Dominios agrupados:** 											 5. Contracts & Legal Terms

9.Compliance & Policy Engine

12\.Workflow, Audit & Legal Evidence

**Por qué esta agrupación:** Un contrato en Bolivia no es solo un papel; es un conjunto de reglas legales vivas. Al unir el Contrato con el Motor de Cumplimiento, el sistema puede validar en tiempo real que el sueldo pactado no sea menor a **Bs 3.300** (SMN 2026) antes de guardar el registro.

- **Explicación técnica:** El motor de cumplimiento actúa como un interceptor dentro del flujo del contrato. Toda adenda o cambio contractual genera automáticamente la evidencia de auditoría y los eventos de aprobación (SoD) necesarios para defensa legal.

**3. BC: Employee Dossier & Talent Foundations (La "Memoria Institucional")**

**Dominios agrupados:**  	               									                     6. Digital Kardex & Document Compliance

- Assets & Equipment Assignment
- Talent Inventory & Learning Records
- **Por qué esta agrupación:** Estos dominios forman el "Expediente Integral" del trabajador. Agruparlos permite que el sistema entienda que el "Talento" de una persona incluye tanto sus títulos académicos como las herramientas tecnológicas (Assets) que tiene a su cargo para ejercer su labor.
- **Explicación técnica:** Centraliza la **Gobernanza Documental**. Permite validar, por ejemplo, que un docente no solo tenga el título (Talento), sino que su carnet sanitario esté vigente (Kardex) para poder dar clases.

**4. BC: Accruals, Seniority & Time-Off (El "Reloj de Beneficios")**

**Dominios agrupados:**											       11. Absences, Leaves & Holiday Management

- Seniority, Benefits & Accrual Tracking
- **Por qué esta agrupación:** Ambos dominios dependen estrictamente del paso del tiempo y de la antigüedad continua. Las vacaciones y el **Quinquenio** son "acumulaciones" que nacen de la misma línea de tiempo laboral.
- **Explicación técnica:** Gestiona todos los **Saldos Acumulables**. Al estar juntos, el sistema puede compensar días de misión de campo (ONG) con descansos o calcular el impacto de una baja médica en el cálculo del Bono de Antigüedad de forma inmediata.

**5. BC: Financial & Social Compliance (El "Módulo de Salidas")**

**Dominios agrupados:** 					                                                                                            10. Budget Allocation & Funding Control

- Social Security & Regulatory Relations
- **Por qué esta agrupación:** Ambos manejan la distribución del costo laboral hacia entes externos (Donantes en ONGs o el Estado en Impuestos/Gestora). El **RC-IVA del 13%** y los aportes de la **Gestora (12,71%)** son, financieramente, una distribución del presupuesto de la empresa.
- **Explicación técnica:** Asegura que la **Imputación Analítica** sea exacta. Si un trabajador de Retail se mueve de sucursal, este contexto ajusta tanto el presupuesto de la tienda (Budget) como la previsión del aporte patronal de salud correspondiente a esa ubicación.

**6. BC: Interaction & Intelligent Experience (La "Capa Externa")**

**Dominios agrupados:** 											         8. Employee & Manager Self-Service (ESS/MSS)

- AI Insights & Predictive Analytics
- **Por qué esta agrupación:** Esta es la "cara" del sistema. La IA consume datos de todos los procesos para entregárselos al usuario final en forma de alertas o recomendaciones proactivas.
- **Explicación técnica:** El ESS/MSS es el canal de entrada y salida de datos, mientras que la IA es el procesador cualitativo. Por ejemplo, el sistema muestra al Gerente (MSS) una predicción de que 5 empleados cumplirán su **Quinquenio** en diciembre, sugiriendo la provisión de fondos.














**1. Contexto: Workforce & Org Master (Núcleo de Identidad)**

**Agregado 1: PersonIdentity**

- **Root:** Person.
  - **Por qué:** Es el ancla de la identidad civil; sobrevive a cualquier contrato o relación laboral.
- **Contenido:**
  - PartyIdentifier (Entity): Se define como **Entity** porque los documentos (CI, Pasaporte) tienen ciclo de vida, fechas de emisión y caducidad que deben rastrearse individualmente.
  - ContactPoint (VO): Es un **Value Object** porque si un email o teléfono cambia, se reemplaza el valor completo; no tiene identidad propia fuera de la persona.

**Agregado 2: EmploymentRelationship**

- **Root:** Relationship.
  - **Por qué:** Es el "puente" legal. Controla si el vínculo es laboral, académico o de pasantía; sin esta raíz, los perfiles operativos no tienen contexto.
- **Contenido:**
  - WorkerProfile / AcademicProfile (Entity): Son **Entities** porque evolucionan (cambios de nivel, atributos operativos) y mantienen una identidad vinculada a la relación laboral específica.
  - StatusLog (Entity): Rastrea la máquina de estados (Onboarding, Activo, Suspendido); requiere identidad para auditoría de transiciones.

**Agregado 3: OrgStructure**

- **Root:** OrgUnit.
  - **Por qué:** Es el nodo central de la jerarquía (Sucursal, Facultad, Departamento). Cualquier cambio estructural nace de aquí.
- **Contenido:**
  - OrgHierarchy (Entity): Define el "padre" y el tipo de relación (Administrativa/Académica); tiene identidad porque las jerarquías se versionan en el tiempo.
  - CostCenter (VO): Es un **Value Object** porque es una etiqueta contable inmutable; si el código cambia, se asigna uno nuevo.

**Agregado 4: PositionPlaza**

- **Root:** Position.
  - **Por qué:** Representa la "silla" física o virtual en la organización; existe independientemente de si hay alguien ocupándola.
- **Contenido:**
  - Job (Reference/VO): Descriptor del cargo; se trata como **VO** o referencia externa porque sus atributos son estandarizados para muchas posiciones.
  - HeadcountPlan (VO): Atributo de límite presupuestario; describe la capacidad de la plaza.
-----
**2. Contexto: Legal & Compliance (Cerebro Normativo)**

**Agregado 5: EmploymentAgreement**

- **Root:** Contract.
  - **Por qué:** Es la entidad legal máxima que dicta las reglas de pago y jornada.
- **Contenido:**
  - ContractAddendum (Entity): Las adendas tienen su propia fecha de firma y vigencia; requieren identidad para el **Effective Dating** .
  - SalaryTerms (VO): Describe el monto y moneda; es **VO** porque cualquier cambio salarial genera un nuevo registro de términos para una fecha específica.
  - ComplianceSnapshot (Snapshot VO): **Snapshot VO** vital para capturar qué leyes estaban vigentes (ej. SMN Bs 3.300) al momento de firmar, protegiendo la retroactividad legal.

**Agregado 6: CompliancePolicy**

- **Root:** PolicyRule.
  - **Por qué:** Define la regla abstracta (ej. "Validación de Salario Mínimo") que el sistema debe ejecutar.
- **Contenido:**
  - LegalThreshold (VO): Valores inmutables como "Bs 3.300" o "12.71%"; si el gobierno decreta un cambio, se crea un nuevo umbral.
-----
**3. Contexto: Dossier & Talent (Memoria Institucional)**

**Agregado 7: DigitalKardex**

- **Root:** DocumentRecord.
  - **Por qué:** Representa la entrada única de un documento legal en el expediente.
- **Contenido:**
  - ValidationStatus (VO): Estado del documento (Aprobado/Rechazado); es un descriptor del momento.
  - DocumentMetadata (VO): Datos del archivo (hash, storage ID); inmutables tras la carga.

**Agregado 8: AssetCustody**

- **Root:** AssignedAsset.
  - **Por qué:** Es el registro de responsabilidad que vincula un activo a una persona; es la raíz para el proceso de liquidación.
- **Contenido:**
  - AssetDescriptor (VO): Características técnicas del equipo asignado.
-----
**4. Contexto: Accruals & Time-Off (Reloj de Beneficios)**

**Agregado 9: AccrualVault**

- **Root:** AccrualBalance.
  - **Por qué:** Es el libro contable de días o dinero acumulado (Vacaciones/Quinquenios).
- **Contenido:**
  - SeniorityMilestone (VO): Eventos fijos en el tiempo (ej. "Cumplió 2 años") que disparan cambios en porcentajes.
  - LeaveTransaction (Entity): Cada solicitud de ausencia tiene identidad, flujo de aprobación y fechas propias.
-----
**5. Contexto: Financial & Social (Egresos y Retenciones)**

**Agregado 10: BudgetFunding**

- **Root:** FundingSource.
  - **Por qué:** Representa la fuente del dinero (Donante X, Proyecto Y); sin esto, no hay imputación analítica.
- **Contenido:**
  - LaborCostSplit (VO): La distribución porcentual (ej. 50% Proyecto A, 50% Proyecto B); debe sumar 100% como regla de integridad.
-----
**6. Contexto: Experience & IA (Capa Inteligente)**

**Agregado 11: PredictiveInsight**

- **Root:** PredictionModel.
  - **Por qué:** Orquesta el análisis de datos para generar alertas proactivas.
- **Contenido:**
  - RiskAlert (VO): Mensaje inmutable de advertencia sobre pasivos laborales (ej. "Alerta: 10 Quinquenios por pagar en 90 días").














**1. Clúster de Políticas: Compensación y Beneficios (Eje Financiero)**

**P1: Política de Base de Cálculo de Antigüedad**

- **Para Universidades/Fundaciones: El cálculo del Bono de Antigüedad se realiza estrictamente sobre la base de un (1) Salario Mínimo Nacional (SMN) de Bs 3.300.**
- **Para Comerciales y Privadas: Se aplica obligatoriamente la base de tres (3) SMN, lo que equivale a Bs 9.900 para la gestión 2026.**
- **Implementación: El sistema invoca automáticamente esta política al detectar aniversarios laborales en el AccrualVault para actualizar el porcentaje correspondiente (5%, 11%, 18%, etc.).**

**P2: Política de Distribución de Utilidades (Primas)**

- **Para Universidades/Fundaciones: Se aplica una política de Exención Total; al ser entidades sin fines de lucro, el sistema anula cualquier provisión o pago de Primas Anuales.**
- **Para Comerciales y Privadas: Se activa la política de Provisión Mensual del 8,33% del salario bajo la premisa proyectada de rentabilidad positiva al cierre del ejercicio.**
- **Regla Técnica: Si el 25% de la utilidad neta no alcanza para un sueldo completo, el sistema debe prorratear el pago equitativamente entre la plantilla.**
-----
**2. Clúster de Políticas: Carga Horaria y Academia (Eje Universitario)**

**P3: Política de Multi-Rol Académico-Administrativo**

- **Solo Universidades: Permite que un individuo posea simultáneamente un WorkerProfile (administrativo) y un AcademicProfile (docente) bajo contratos distintos.**
- **Regla Técnica: La política valida que la suma de horas no colisione físicamente y que la asignación presupuestaria sea coherente entre facultades o proyectos.**

**P4: Política de Contratación por Carga Horaria (Materia/Bloque)**

- **Solo Universidades: Los contratos docentes se rigen por la política de Vigencia Semestral/Materia.**
- **Regla Técnica: El sistema automatiza la suspensión o reactivación del contrato según el calendario académico, manteniendo la antigüedad acumulada en el escalafón docente.**
-----
**3. Clúster de Políticas: Jornada y Tiempo Legal (Eje de Cumplimiento)**

**P5: Política de Límite de Jornada por Género**

- **Universal (Bolivia 2026): El sistema bloquea cualquier planificación que exceda las 48 horas semanales para varones y las 40 horas semanales para mujeres y menores.**
- **Acción del Sistema: Si se detecta un exceso en la programación, el sistema emite una advertencia o reclasifica el excedente como Horas Extraordinarias con el 100% de recargo.**

**P6: Política de Recargo Nocturno y Dominical**

- **Comerciales (Retail): Identificación automática de turnos entre las 20:00 y las 06:00 para aplicar recargos del 25% al 50% según el sector.**
- **Privadas/Universidades: El trabajo en domingo debe ser compensado con un día de descanso en la semana posterior o remunerado con recargo del 100% si se prescinde del descanso.**
-----
**4. Clúster de Políticas: Ciclo de Vida y Documentación (Eje Administrativo)**

**P7: Política de Vigencia Documental Crítica (Compliance)**

- **Retail: Exige carnet sanitario emitido por autoridades de salud y certificados de antecedentes policiales vigentes para manejo de valores.**
- **Universidades: Exige validación de títulos académicos y certificados de escalafón para la asignación de materias.**
- **Implementación: El DigitalKardex bloquea el estado "Activo" o emite alertas de caducidad con escalamiento a gerencia.**

**P8: Política de Alerta Temprana de Quinquenio e Indemnización**

- **Universal: Al detectar 60 meses de antigüedad continua, se consolida el derecho al pago de la Indemnización Acumulada (1 sueldo por año).**
- **Regla Técnica: Tras la solicitud del empleado, la empresa tiene 30 días calendario para el pago; de lo contrario, el sistema autocalcula una multa del 30% del valor adeudado. La indemnización se provisiona contablemente desde el día 91 de relación laboral.**
-----
**5. Clúster de Territorialidad: Santa Cruz (Eje Regional)**

**P9: Política de Aporte Patronal INFOCAL Santa Cruz**

- **Comerciales y Privadas: Aplicación del aporte del 1% calculado sobre el salario mensual de todos los trabajadores a favor de la Fundación INFOCAL Santa Cruz.**
- **Universidades/Fundaciones: La política se desactiva mediante un *toggle switch* en la configuración del tenant al ser entidades comúnmente no obligadas.**

**P10: Política de Feriados Regionales y Calendario Cruceño**

- **Universal: Automatización del feriado departamental del 24 de septiembre y feriados nacionales.**
- **Regla: El sistema hereda estos calendarios para el cálculo automático de recargos por feriado trabajado.**
-----
**6. Clúster de Movilidad y Campo (Eje ONG/Universidades)**

**P11: Política de Viáticos, Misiones de Campo y Rendición**

- **Universidades/Fundaciones: Gestión de memorandos de viaje y automatización de reglas para días de descanso compensatorio tras actividades de fin de semana.**
- **Regla Técnica: Los viáticos no forman parte del total ganado para aportes, pero el sistema debe rastrear el descargo de fondos para auditorías.**
-----
**7. Clúster de Protección Contractual (Eje Legal)**

**P12: Política de Prevención de Tácita Reconducción**

- **Universal (Enfoque ONG/Educación): El sistema emite alertas con 90 días de anticipación al vencimiento de contratos a plazo fijo.**
- **Consecuencia Técnica: Evita que el contrato pase a ser indefinido por omisión, lo cual dispararía la obligación de indemnización por tiempo indefinido.**
-----
**8. Clúster de Acumulación de Beneficios (Eje Accruals)**

**P13: Política de Escala de Vacaciones**

- **Universal: El AccrualVault calcula la disponibilidad de días según la escala:**
  - **1 a 5 años: 15 días hábiles.**
  - **5 a 10 años: 20 días hábiles.**
  - **10 años en adelante: 30 días hábiles.**
- **Regla Técnica (Educación): En el perfil docente, el sistema permite configurar el "Receso Académico" como consumo colectivo de vacaciones, bloqueando solicitudes individuales durante el semestre.**

**P14: Política de Mantenimiento de Valor (UFV)**

- **Universal: El sistema lleva contabilidad del crédito fiscal (RC-IVA) y provisiones de beneficios sociales, trasladando saldos con mantenimiento de valor según la cotización de la UFV.**
-----
**9. Clúster Fiscal y de Liquidación (Eje Finiquito)**

**P15: Política de Base de Cálculo (Promedio Indemnizable)**

- **Universal: La base para el cálculo de Aguinaldo, Indemnización y Desahucio es el promedio del "Total Ganado" de los últimos tres (3) meses anteriores al cese o pago.**
- **Retail: Para trabajadores con comisiones, el promedio incluye obligatoriamente todas las variables percibidas en el trimestre.**

**P16: Política de Aguinaldo y Duodécimas**

- **Universal: Provisión mensual obligatoria (8,33%) para el pago del Aguinaldo.**
- **Regla de Cumplimiento: Si el pago no se registra hasta el 20 de diciembre, el sistema habilito automáticamente el cálculo de Pago Doble por infracción social.**

**P17: Política de Finiquito y Desahucio**

- **Universal: En caso de desvinculación, el sistema tiene un plazo máximo de 15 días calendario para emitir el pago; tras este plazo, se activa una multa automática del 30% sobre el saldo total.**
- **Regla de Desahucio: Si el motivo de baja es "Despido sin Causa", el sistema añade automáticamente 3 salarios promedio al finiquito. En "Renuncia Voluntaria", el desahucio se anula.**

**P18: Política de Aplicación de "IVA Transparente" (RC-IVA 13%)**

- **Universal: Implementación del procedimiento escalonado: Sueldo Neto menos dos (2) SMN de exención, aplicado a la alícuota plana del 13% real.**
- **Deducción Adicional: Resta invariable del 13% de un (1) SMN (Bs 429) por presunción de compras**








**1. Workflow de Onboarding Integral (Alta de Colaborador)**

- **Alcance:** Dominios 1, 2, 3, 4, 5, 6.
- **Agregados involucrados:** PersonIdentity, EmploymentRelationship, EmploymentAgreement, PositionPlaza.
- **Descripción Técnica:** Es el proceso más complejo. Debe crear la identidad, definir el vínculo, asignar la plaza y formalizar el contrato.
- **Variabilidad por Tenant:**
  - **Retail:** Flujo masivo optimizado para velocidad.
  - **ONG:** Bloquea el proceso si no se asocia un código de donante (P10).
  - **Universidades:** Requiere la validación previa de títulos académicos (P7) para habilitar el perfil docente.

El **Onboarding Integral** es el proceso core de orquestación de dominios que transforma una identidad civil en un recurso operativo y legal dentro de la organización. Su objetivo es garantizar que ninguna persona sea activada sin cumplir con los cuatro pilares: Identidad validada, Vínculo definido, Plaza presupuestada y Contrato legalmente conforme (Piso salarial SMN 2026).

**Objetivos e Invariantes Críticas**

- **Invariante de Identidad:** No se permiten PersonIdentity duplicados (Validación por CI/Pasaporte).
- **Invariante de Plaza:** No se puede asignar una Position si el HeadcountPlan está al 100% de ocupación.
- **Invariante de Cumplimiento (Piso Salarial):** El SalaryTerms no puede ser < Bs 3.300.
- **Invariante de Financiamiento (ONG):** Bloqueo total si la FundingSource no tiene saldo o no existe.
- **Invariante Académica (Educación):** El AcademicProfile requiere títulos validados antes de la firma del contrato.

**2. Detalle del Flujo (Happy Path & Edge Cases)**

1. **Inicio de Proceso:** El reclutador o sistema externo inicia la solicitud.
1. **Validación de Identidad (Deduplicación):**
   1. Si el identificador (CI) existe en el histórico: Se recupera la Person (Reingreso) y se dispara PERSON\_DEDUPLICATION\_MATCH\_FOUND.
   1. Si es nuevo: Se crea PersonIdentity (Estado: Draft).
1. **Definición de Relación y Perfil:** Se crea EmploymentRelationship.
   1. **Si es Educación:** Se debe crear el AcademicProfile y solicitar documentos de grado.
1. **Selección y Bloqueo de Plaza:** Se vincula la Position.
   1. Si la plaza no está vacante: Error POSITION\_NOT\_VACANT.
   1. **Si es ONG:** El sistema exige el ProjectID/DonorID. Si no se asocia, el flujo se detiene.
1. **Generación de Propuesta Contractual (Effective Dating):** Se crea el EmploymentAgreement con effective\_from (fecha futura de ingreso).
   1. Validación de Regla P9: Si Sueldo < Bs 3.300, se dispara CONTRACT\_LEGAL\_PISO\_VIOLATED y se bloquea el guardado.
1. **Carga Documental y Compliance:** El DigitalKardex marca los documentos obligatorios según el perfil.
   1. **Si es Retail:** Se exige Carnet Sanitario.
   1. Si faltan documentos: El estado de la relación pasa a "Pending Compliance".
1. **Validación y Firma:** Revisión por SoD (Segregación de Funciones). Un usuario diferente al creador debe aprobar la propuesta.
1. **Activación:** Al llegar la effective\_from, el sistema cambia el estado a **Activo** y dispara los eventos de notificación a nómina y seguridad social.

4\. Justificación Técnica

Arquitectura Temporal: El uso de effective\_from permite que el Onboarding ocurra días o semanas antes del ingreso real sin afectar la planilla actual, pero permitiendo la planificación del Headcount.

Aislamiento por Tenant: Las reglas de negocio (Piso salarial, INFOCAL, Donantes) se inyectan mediante un patrón de Estrategia (Strategy Pattern) en el motor de validación. Esto evita que el código de "Retail" ensucie la lógica de "Universidades".

Consistencia Eventual: El uso de eventos como ONBOARDING\_COMPLETED permite que módulos secundarios (como el de Activos o Accesos) reaccionen sin acoplamiento directo con el Core HR.



**2. Workflow de Modificación Contractual (Adendas)**

- **Alcance:** Dominios 5, 9, 12.
- **Agregados involucrados:** EmploymentAgreement, CompliancePolicy.
- **Descripción Técnica:** Gestiona cualquier cambio en sueldo, cargo o jornada.
- **Política Crítica:** Dispara la **P1 (Base de Antigüedad)** y la **P13 (RC-IVA)** para validar que el nuevo haber neto sea legal.
- **Invariante:** Implementa **SoD (Segregación de Funciones)**; el analista que propone el aumento no puede ser quien lo apruebe.

**Definición**

Proceso de orquestación para alterar las condiciones de la EmploymentRelationship sin romper la continuidad laboral. Su objetivo es garantizar que cualquier cambio en salario, cargo o jornada cumpla con el motor de políticas (SMN y RC-IVA) y respete la **Segregación de Funciones (SoD)**.

**2. Detalle del Flujo**

1. **Solicitud de Cambio:** El usuario (HR o Gerente) inicia la propuesta de adenda.
1. **Validación de Políticas (Interceptor de Cumplimiento):**
   1. **P1 (Base Antigüedad):** Si el Tenant es ONG, valida base 1 SMN; si es Corp/Retail, base 3 SMN ($3 \times 3.300 = 9.900$).
   1. **P13 (RC-IVA):** Calcula el sueldo neto proyectado. Si el aumento dispara una retención impositiva que el empleado no esperaba, el sistema genera un "Aviso de Impacto Tributario".
   1. **Piso Salarial:** Bloqueo si el nuevo salario es $< 3.300$.
1. **Control de SoD (Invariante):** El sistema bloquea al User\_ID creador para la acción de aprobación.
1. **Bifurcación de Aprobación:**
   1. **Si el cambio es $> 15\%$ salarial:** Requiere aprobación de Finanzas (Budget Controller).
   1. **Si es cambio de cargo:** Valida si la nueva Position tiene plaza disponible (HeadcountPlan).
1. **Firma y Aplicación:** Se genera el documento digital. Al firmarse, se crea una nueva versión del EmploymentAgreement con su respectivo effective\_from.
### **4. Justificación Técnica**
Se utiliza el patrón **State** para gestionar el ciclo de vida de la adenda (Draft -> Validating -> Pending\_Approval -> Effective). La lógica de cálculo se desacopla en un ComplianceService para permitir actualizaciones de leyes sin desplegar de nuevo el flujo de trabajo.



**3. Workflow de Renovación Preventiva (Evitar Tácita Reconducción)**

- **Alcance:** Dominios 5, 16.
- **Agregados involucrados:** EmploymentAgreement, PredictiveInsight.
- **Descripción Técnica:** Se activa automáticamente 90 días antes del vencimiento de un contrato a plazo fijo.
- **Propósito:** Obligar a una decisión (Renovar o Terminar) para evitar que el contrato pase a ser indefinido por omisión legal (P12). Especialmente crítico en **ONGs** y **Educación** por periodos académicos.
### **Definición**
Workflow proactivo orquestado por el dominio de **Predictive Insights**. Su objetivo es mitigar el riesgo de que un contrato a plazo fijo se transforme en indefinido por omisión administrativa (Art. 12 LGT).
### **2. Detalle del Flujo**
1. **Disparo por Cron (T-90 días):** El sistema identifica contratos por vencer.
1. **Generación de Alerta:** Se dispara el evento de riesgo a RRHH y al Jefe Directo.
1. **Evaluación de Continuidad:**
   1. **Opción A (Renovación):** Se inicia el Workflow de Adenda para extender el plazo (Solo permitido hasta 2 veces en Bolivia).
   1. **Opción B (Terminación):** Se inicia el Workflow de Offboarding para cumplir con el preaviso legal.
1. **Validación de Límite Legal:** Si el contrato ya ha sido renovado 2 veces, el sistema bloquea la opción de "Plazo Fijo" y obliga a convertir a "Indefinido".
1. **Cierre de Ciclo:** Si a falta de 15 días no hay decisión, el sistema escala la alerta a "Nivel Crítico" con copia a Legal.



**4. Workflow de Transferencia y Movilidad Territorial**

- **Alcance:** Dominios 1, 10.
- **Agregados involucrados:** OrgStructure, BudgetFunding.
- **Descripción Técnica:** Gestiona el cambio de un empleado entre sucursales o centros de costo.
- **Variabilidad Retail:** Activa la **P10** para prorratear el costo laboral entre tiendas según los días trabajados en cada una (Split 100%).

Definición

Gestiona el movimiento de un colaborador entre nodos de la estructura (OrgUnit) dentro del departamento de Santa Cruz. Asegura que la Imputación Analítica siga al empleado y que se respeten los presupuestos locales de cada sucursal o campus.

2\. Detalle del Flujo

Solicitud de Movimiento: Un Gerente de Tienda (Retail) o Decano (Educación) solicita el traslado del empleado a una nueva OrgUnit.

Validación de Presupuesto Local: El sistema interroga el BudgetFunding de la unidad destino.

Si es Educación: Valida que la carga horaria en el nuevo Campus no exceda el tope del AcademicProfile.

Configuración de Split de Costos (Invariante del 100%):

Si la transferencia ocurre a mitad de mes, el sistema genera automáticamente un prorrateo en el LaborCostSplit para que la nómina se divida exactamente según los días trabajados en cada unidad.

Preservación de Beneficios Regionales (Santa Cruz):

Invariante: Al no salir de Santa Cruz, el switch de INFOCAL (1%) permanece activo (para Retail/Corp) o inactivo (para ONG) según la naturaleza del Tenant, sin cambios por la ubicación física.

Calendario: Se mantienen los feriados departamentales (24 de Sep) inalterables.

Logística de Activos en Sede: Se genera una orden de verificación para el AssetCustody. ¿El equipo (ej. laptop, llaves de oficina) se queda en la sucursal origen o se mueve con el empleado?

4\. Justificación Técnica

Al limitar el alcance a Santa Cruz, eliminamos la complejidad tributaria inter-departamental, pero elevamos la precisión en la Imputación de Costos. El sistema garantiza que si un empleado de Retail se mueve de una tienda en la "Pampa de la Isla" a una en el "Equipetrol", la rentabilidad (P&L) de cada tienda refleje exactamente el costo de su mano de obra.



**5. Workflow de Verificación de Compliance Documental**

- **Alcance:** Dominio 6, 9.
- **Agregados involucrados:** DigitalKardex.
- **Descripción Técnica:** Orquesta la carga, validación y auditoría de documentos obligatorios.
- **Política Crítica:** Ejecuta la **P7 (Vigencia Documental)**. Si un carnet sanitario (Retail) o un título docente vence, el sistema suspende automáticamente la elegibilidad para turnos.

Definición

Este flujo orquesta el ciclo de vida de la evidencia documental necesaria para habilitar la operatividad del trabajador. Su objetivo es garantizar que ningún colaborador realice funciones críticas sin el respaldo legal vigente, actuando como un "Gatekeeper" automático que comunica el estado de cumplimiento al motor de turnos y nómina.

2\. Detalle del Flujo (Happy Path & Edge Cases)

Gatillo de Requerimiento:

Alta: Al crear una EmploymentRelationship, el sistema identifica documentos obligatorios según el Tenant (P7).

Renovación: Un proceso en segundo plano (Cron Job) detecta documentos próximos a vencer (T-30 días).

Carga de Documento (ESS o Adm): El empleado o analista sube el archivo. El sistema genera metadatos (Hash SHA-256) para asegurar la inalterabilidad en el DigitalKardex.

Proceso de Validación:

Estado: Pending\_Review. El documento entra en la cola de auditoría de RRHH.

Si es validado: Cambia a Approved y se registra la expiry\_date.

Si es rechazado: Cambia a Rejected y se notifica al empleado el motivo (ej. "Imagen ilegible").

Monitoreo de Vigencia (P7 - Policy Engine):

El motor de cumplimiento evalúa diariamente la expiry\_date.

Suspensión Automática (Invariante de Seguridad):

Si un documento crítico (ej. Carnet Sanitario en Retail o Título habilitante en Educación) llega a su fecha de vencimiento sin renovación aprobada:

Acción: El sistema cambia el ValidationStatus a Expired y dispara la suspensión de elegibilidad.

Bloqueo de Operaciones: El dominio de Scheduling (Turnos) recibe la notificación y elimina al trabajador de la grilla de turnos activa hasta que se regularice el documento.

Restauración de Elegibilidad: Al aprobarse un nuevo documento, el sistema reactivo automáticamente la capacidad de ser asignado a turnos.

4\. Justificación Técnica

Desacoplamiento Operativo: El DigitalKardex no detiene el contrato, pero sí la elegibilidad operativa. Esto permite que el empleado siga en planilla (devengando beneficios de antigüedad) pero no pueda trabajar en áreas donde la ley exige el documento vigente (ej. manipulación de alimentos en Retail).

Gobernanza de Datos: La inalterabilidad mediante Hash garantiza que, ante una inspección del Ministerio de Trabajo en Santa Cruz, la empresa posea evidencia forense de qué documento estaba vigente y cuándo fue validado.

Automatización de Riesgo: Elimina el error humano de permitir trabajar a alguien con documentos vencidos, lo cual suele derivar en multas de sanidad o educación.



**6. Workflow de Gestión de Ausencias y Vacaciones**

- **Alcance:** Dominio 11, 8 (ESS/MSS).
- **Agregados involucrados:** AccrualVault.
- **Descripción Técnica:** Flujo de solicitud desde el autoservicio hasta la aprobación jerárquica.
- **Regla:** Valida saldos acumulados (P15) y feriados locales de Santa Cruz (P10) antes de autorizar el permiso.

6\. Workflow de Gestión de Ausencias y Vacaciones

1\. Definición

Este flujo gestiona el ciclo de vida de las solicitudes de tiempo libre (vacaciones, permisos, bajas) desde el Self-Service. Su objetivo es garantizar que cada ausencia esté respaldada por un saldo real en el AccrualVault, respetando la escala de antigüedad boliviana y protegiendo la continuidad operativa mediante aprobaciones jerárquicas.

2\. Detalle del Flujo (Happy Path & Edge Cases)

Consulta de Saldo (ESS): El empleado visualiza su disponibilidad en tiempo real. El AccrualVault calcula el saldo basado en la P13 (Escala de Vacaciones):

1 a 5 años: 15 días hábiles.

5 a 10 años: 20 días hábiles.

10 años en adelante: 30 días hábiles.

Configuración de la Solicitud: El usuario selecciona fechas y tipo de ausencia.

Validación Intersectorial de Calendario (P10 - Santa Cruz):

El sistema cruza las fechas con el calendario de feriados de Santa Cruz (ej. 24 de septiembre).

Lógica: Si la vacación incluye el 24 de septiembre, ese día no se descuenta del saldo, ya que es un feriado departamental no hábil.

Validación de Saldo (Invariante):

Si Saldo < Días Solicitados: Se dispara VACATION\_BALANCE\_THRESHOLD\_LOW. El sistema bloquea el envío (salvo política de anticipo configurada).

Si Saldo OK: Se crea la LeaveTransaction en estado Pending.

Notificación y Aprobación (MSS): El supervisor recibe la notificación.

Si el supervisor rechaza: Debe ingresar un motivo. La transacción pasa a Rejected.

Si el supervisor aprueba: La transacción pasa a Approved.

Afectación del Vault: Al aprobarse, el sistema realiza el "asiento contable" de salida en el AccrualVault, disminuyendo el saldo disponible de forma irrevocable para auditoría.

Sincronización con Nómina: El evento de aprobación notifica al motor de Payroll para el cálculo de primas o descuentos si correspondiera.

4\. Justificación Técnica

Gestión de Saldos como Libro Mayor: El AccrualVault no guarda solo un número, sino un historial de transacciones (LeaveTransaction). Esto permite auditorías ante el Ministerio de Trabajo sobre por qué un empleado tiene X días de saldo en una fecha específica.

Inyección de Calendarios Regionales: Al estar el alcance limitado a Santa Cruz, el sistema inyecta el calendario departamental por defecto. Esto evita errores manuales donde el encargado de RRHH olvida excluir los feriados locales del conteo de vacaciones.

Segregación de Responsabilidades: El ESS/MSS desacopla la carga administrativa de RRHH, permitiendo que la validación técnica sea automática y la validación operativa sea del jefe directo.



**7. Workflow de Consolidación y Pago de Quinquenio**

- **Alcance:** Dominios 13, 12, 8.
- **Agregados involucrados:** AccrualVault, EmploymentAgreement.
- **Descripción Técnica:** Se dispara al cumplir 60 meses de antigüedad.
- **Invariante:** Monitorea el plazo de 30 días calendario para el pago; si se excede, autocalcula la multa del 30% (P8).

  Definición

  Este workflow gestiona el derecho adquirido del trabajador a cobrar su indemnización acumulada tras cumplir 5 años (60 meses) de servicio continuo, según el DS 522. Su objetivo es automatizar la detección de la elegibilidad, calcular el monto basado en promedios históricos y, fundamentalmente, monitorear el cumplimiento del plazo legal de pago para evitar la penalidad del 30%.

 2. Detalle del Flujo (Happy Path & Edge Cases)1. Detección de Hito (Seniority Milestone):El motor de AccrualVault identifica que el trabajador ha cumplido 60 meses de antigüedad ininterrumpida.Se dispara el evento QUINQUENIO_ELIGIBILITY_REACHED.2. Notificación de Disponibilidad (ESS):El sistema habilita en el portal del empleado la opción "Solicitar Pago de Quinquenio".Nota: El pago es facultativo; el empleado puede decidir no cobrarlo y seguir acumulando (aunque contablemente se provisiona).3. Solicitud Formal:El empleado firma digitalmente la solicitud. El sistema registra la Fecha de Recepción, la cual es el "Día 0" para el cronómetro legal.4. Cálculo de la Base Indemnizable:El sistema accede al histórico de los últimos 90 días (3 meses) de Total Ganado.Calcula el promedio:$$\text{Promedio} = \frac{\sum_{i=1}^{3} \text{Total Ganado}_i}{3}$$Monto a pagar = $\text{Promedio} \times 5$.5. Validación de Invariantes Legales:P8: El sistema verifica que no se realicen deducciones de ley (Gestora/AFP o Impuestos) sobre este monto, ya que es una indemnización.6. Monitoreo del Plazo de Pago (Cronómetro de 30 días):Escenario A (Pago en plazo): Tesorería liquida antes del día 30. El workflow termina con éxito.Escenario B (Mora Legal): Si al llegar al día 31 no se ha registrado el comprobante de pago:El sistema autocalcula una multa del 30% sobre el monto total:$$\text{Total con Multa} = \text{Monto Quinquenio} \times 1.30$$Se dispara el evento QUINQUENIO_PAYMENT_OVERDUE.7. Cierre y Asiento Contable:Se actualiza el AccrualVault reseteando el contador de quinquenios pero preservando la antigüedad total para el Bono de Antigüedad.

4\. Justificación Técnica

Gestión de Riesgo Financiero: La inclusión de un cronómetro activo con alerta de multa protege a la empresa de Santa Cruz de sobrecostos innecesarios por negligencia administrativa.

Integridad de Antigüedad: Es vital que el sistema entienda que el pago de un quinquenio no corta la antigüedad para efectos de las escalas de vacaciones o bonos de antigüedad; solo liquida el monto de la indemnización por ese periodo.

Transparencia (ESS): Al permitir que el empleado vea el cálculo de su promedio de los últimos 3 meses, se reducen las fricciones y consultas manuales al área de compensaciones.



**8. Workflow de Asignación y Recuperación de Activos (Custodia)**

- **Alcance:** Dominio 7.
- **Agregados involucrados:** AssetCustody.
- **Descripción Técnica:** Registra la entrega de equipos (Laptop, Vehículo) y su estado.
- **Control de Salida:** Bloquea el cierre administrativo del empleado si existen activos pendientes de devolución.

Definición

Este workflow gestiona la responsabilidad material del trabajador sobre los bienes de la empresa (laptops, vehículos, herramientas, llaves). Su objetivo es garantizar la trazabilidad de la custodia desde el alta hasta la desvinculación, actuando como un candado administrativo que impide el cierre del finiquito si existen activos pendientes de devolución o reportes de daños no conciliados.

**Detalle del Flujo (Happy Path & Edge Cases)**

1. **Detección de Necesidad de Asignación:**
   1. Gatillo: Un nuevo Onboarding o un cambio de Position que requiere herramientas específicas.
1. **Registro de Entrega (Asignación):**
   1. El gestor de activos selecciona el ítem del inventario y lo vincula al WorkerID.
   1. Se genera un **Acta de Entrega Digital** con el estado físico inicial (ej. "Nuevo", "Usado - Buen estado").
   1. El empleado firma digitalmente en el ESS. Se dispara ASSET\_LOANED\_TO\_WORKER.
1. **Mantenimiento y Auditoría Periódica:**
   1. El sistema permite registrar "Inspecciones de Estado". Si el activo se reporta como dañado, se actualiza el AssetDescriptor.
1. **Inicio de Recuperación (Gatillo de Offboarding):**
   1. Al iniciarse un proceso de baja, el sistema interroga al agregado AssetCustody.
   1. **Invariante de Salida:** Si la lista de AssignedAsset no está vacía, el estado del Offboarding se marca como **"Blocked by Assets"**.
1. **Proceso de Devolución:**
   1. El empleado entrega los equipos. El gestor valida el estado.
   1. **Escenario A (Todo OK):** Se registra la devolución exitosa. Se dispara ASSET\_RETURNED.
   1. **Escenario B (Activo Dañado/Extraviado):** Se registra el incidente. El sistema calcula el valor residual para posible descuento legal (si existe autorización firmada previa).
1. **Liberación de Bloqueo:**
   1. Una vez que el saldo de activos asignados es **cero**, el sistema libera el flujo de "Cierre Administrativo" para proceder al cálculo del Finiquito.

Justificación Técnica

Integridad del Patrimonio: En empresas de Retail (donde hay terminales móviles de venta) o Educación (laptops docentes), la pérdida de activos es un gasto operativo alto. Este workflow automatiza la recuperación sin depender de la memoria del analista de RRHH.

Soporte Legal para Descuentos: Según la normativa boliviana, no se puede descontar arbitrariamente del finiquito. Este flujo genera la evidencia documental (Acta de entrega vs. Acta de devolución con daños) necesaria para sustentar legalmente cualquier retención por daños culposos.

Control Multi-Sede (Santa Cruz): Permite que un empleado devuelva el equipo en una sucursal diferente a la de origen (ej. entregó en el Plan 3000 pero devuelve en Equipetrol) manteniendo la trazabilidad centralizada.



**9. Workflow de Offboarding y Liquidación (Finiquito)**

- **Alcance:** Dominios 3, 5, 14.
- **Agregados involucrados:** EmploymentRelationship, EmploymentAgreement, AccrualVault.
- **Descripción Técnica:** Proceso de baja definitiva que calcula promedios de los últimos 90 días, duodécimas de aguinaldo (P15) e indemnizaciones.

1\. Definición

Este workflow orquesta la desvinculación definitiva del trabajador y el cálculo de sus beneficios sociales de ley. Su objetivo es extinguir el vínculo laboral (EmploymentRelationship) garantizando que el pago final sea exacto, cumpla con los plazos del Ministerio de Trabajo (15 días) y consolide todos los saldos pendientes del AccrualVault.

2\. Detalle del Flujo (Happy Path & Edge Cases)

Gatillo de Desvinculación: Se registra el motivo de baja (Renuncia Voluntaria, Despido Intempestivo, Vencimiento de Contrato o Despido con Causa Justificada).

Validación de Candado de Activos (Invariante Workflow 8):

El sistema consulta el AssetCustody.

Si existen activos: El flujo se detiene en estado Blocked\_by\_Assets. Se debe completar la devolución para proceder.

Si está limpio: El flujo avanza a Calculation\_Pending.

Cálculo del Promedio Indemnizable (P15):

El motor extrae el "Total Ganado" de los últimos 3 meses anteriores al mes de baja.

Variabilidad Retail: El promedio incluye obligatoriamente comisiones y recargos nocturnos percibidos en ese trimestre.

Liquidación de Conceptos (P17):

Indemnización: 1 sueldo promedio por cada año trabajado (y duodécimas por meses/días).

Aguinaldo: Cálculo de duodécimas del año en curso (ene-fecha de baja).

Vacaciones: Conversión del saldo remanente del AccrualVault en monto monetario.

Desahucio: Si el motivo es "Despido sin Causa", el sistema añade automáticamente 3 salarios promedio.

Revisión Legal y Tributaria:

Se calcula el Finiquito Neto.

RC-IVA (P18): Se compensan saldos de crédito fiscal pendientes en el SIAT mediante el mantenimiento de valor UFV.

Cronómetro de Pago (15 días calendario):

Se registra la fecha de baja como "Día 0".

Escenario A (Pago oportuno): El pago se registra antes del día 15. Se emite el formulario de finiquito para firma.

Escenario B (Mora): Al día 16, el sistema autocalcula una multa del 30% sobre el total líquido pagable y dispara FINIQUITO\_PAYMENT\_OVERDUE.

Cierre de Vínculo: La Relationship pasa a estado Terminated y la Position queda marcada como Vacant.

**Justificación Técnica**

- **Gestión de Plazos:** La normativa boliviana es punitiva con los retrasos. Automatizar el cálculo de la multa del 30% asegura que la empresa de Santa Cruz provisione el riesgo real si el proceso administrativo se ralentiza.
- **Consistencia de Datos:** Al conectar el AccrualVault con el Finiquito, eliminamos el riesgo de pagar de más o de menos por días de vacaciones "olvidados".
- **Mantenimiento de Valor (UFV):** El sistema debe asegurar que el crédito fiscal del empleado sea trasladado correctamente al momento de la liquidación, evitando reparos tributarios.



**10. Workflow de Mérito y Escalafón Docente (Solo Educación)**

- **Alcance:** Dominio 15.
- **Agregados involucrados:** PersonIdentity (Academic Profile).
- **Descripción Técnica:** Específico para universidades. Gestiona la subida de nivel salarial o de categoría docente basada en la acumulación de certificados y años de servicio (P3, P4).

Definición

Este workflow es exclusivo para el Tenant de Educación (Universidades y Colegios Privados en Santa Cruz). Su objetivo es gestionar el ascenso de categoría salarial y académica del personal docente basándose en la acumulación de méritos (títulos, investigaciones, certificaciones) y años de servicio. Transforma el crecimiento cualitativo del docente en un impacto cuantitativo en su estructura salarial.

2\. Detalle del Flujo (Happy Path & Edge Cases)

Detección de Elegibilidad (Gatillo Proactivo o Manual):

Automático: El sistema detecta que el docente ha cumplido los años de servicio requeridos para subir de categoría (ej. paso de Auxiliar a Adjunto). Se dispara RANK\_UPGRADE\_ELIGIBILITY\_REACHED.

Manual (ESS): El docente sube un nuevo título de postgrado (Maestría/Doctorado) al DigitalKardex.

Carga de Evidencias y Postulación: El docente inicia la solicitud desde su perfil académico. Debe adjuntar los certificados que sustenten el ascenso.

Validación de Títulos (P7):

El sistema exige la validación de autenticidad del título.

Si el título no está validado: El flujo se detiene hasta que el administrador de Kardex emita el evento DOCENT\_ACADEMIC\_TITLE\_VERIFIED.

Evaluación de Comisión Académica (MSS):

La solicitud llega al Decanato o Dirección Académica.

Se evalúa si los méritos cumplen con la P3 (Multi-Rol) y los reglamentos internos de la institución.

Aprobación de Cambio de Rango:

Si es rechazado: Se notifica al docente con las observaciones.

Si es aprobado: El sistema actualiza el campo Rank en el AcademicProfile y dispara ACADEMIC\_PROFILE\_RANK\_UPDATED.

Actualización Salarial Automática:

El cambio de rango invoca al Workflow 2 (Adenda).

Se propone el nuevo haber básico según la escala salarial ligada a la nueva categoría.

Sincronización con Escalafón: Se registra el nuevo hito en el historial del docente para futuros cálculos de antigüedad académica.

**Justificación Técnica**

- **Especialización de Entidad:** Al separar el WorkerProfile (administrativo) del AcademicProfile (docente), permitimos que un mismo individuo en Santa Cruz tenga un sueldo fijo por sus horas de oficina y un sueldo variable/escalafonado por sus horas de cátedra (P3), sin cruzar bases imponibles de forma errónea.
- **Integridad Académica:** El flujo asegura que ningún docente suba de nivel salarial sin que el DigitalKardex confirme la existencia de títulos habilitantes, cumpliendo con las normativas del Ministerio de Educación de Bolivia.
- **Automatización de Carrera:** Reduce la carga operativa de las facultades al notificar automáticamente cuándo un docente es apto para subir de categoría, incentivando la retención de talento calificado.



**11. Workflow de Actualización de Datos Personales (ESS/MSS)**

- **Propósito:** Permitir que el empleado solicite cambios en su dirección, estado civil o cuentas bancarias.
- **Justificación:** Empodera al trabajador, pero mantiene el control de RRHH, quien debe validar el respaldo documental antes de impactar el **Agregado PersonIdentity**.\

Este flujo de **Autoservicio (ESS)** permite que el colaborador mantenga la vigencia de su información civil, domiciliaria y financiera de forma descentralizada. Su objetivo es garantizar la calidad de los datos maestros en el Agregado PersonIdentity, asegurando que cualquier cambio sensible (como cuentas bancarias para el abono de haberes en Santa Cruz) pase por un filtro de validación humana y documental antes de impactar el registro oficial.
### **2. Detalle del Flujo (Happy Path & Edge Cases)**
1. **Inicio de Solicitud (ESS):** El empleado accede a su perfil y edita campos específicos (Dirección, Teléfono, Estado Civil o Datos Bancarios).
1. **Clasificación de Sensibilidad:**
   1. **Datos Menores (ej. Teléfono):** El sistema permite la actualización directa o con notificación simple (según configuración del Tenant).
   1. **Datos Críticos (ej. Cuenta Bancaria, Estado Civil):** El sistema marca la solicitud como **"Pending Validation"** y activa el requisito de evidencia.
1. **Carga de Evidencia Documental:**
   1. Para cambio de cuenta: Certificación bancaria o captura de banca móvil.
   1. Para estado civil: Certificado de matrimonio o sentencia de divorcio.
   1. Se dispara el evento DOCUMENT\_RECORDED vinculado a la solicitud.
1. **Notificación a RRHH (MSS/Backoffice):** El Analista de Datos Personales recibe la tarea de revisión en su bandeja de entrada.
1. **Auditoría de Veracidad:**
   1. El analista compara el documento cargado contra los nuevos datos ingresados.
   1. **Si los datos no coinciden con el respaldo:** Se rechaza la solicitud. Se dispara DATA\_CHANGE\_REQUEST\_REJECTED.
   1. **Si los datos son correctos:** Se procede a la aprobación.
1. **Actualización del Agregado:**
   1. Al aprobar, el sistema escribe los nuevos valores en PersonIdentity.
   1. Se preserva el historial (versionado) del dato anterior para fines de auditoría retroactiva.
   1. Se dispara el evento PERSON\_UPDATED.
1. **Sincronización con Nómina:** Si el cambio fue en la cuenta bancaria, se envía una señal al módulo de **Financial & Social** para actualizar la dispersión de fondos del próximo periodo

Justificación Técnica

Gobierno de Datos (Data Governance): Evita la degradación de la base de datos por errores de transcripción del usuario. Al requerir aprobación para datos financieros, se eliminan errores en la transferencia de salarios (rechazos bancarios).

Auditoría de Cambios: En un entorno Enterprise, es inaceptable que un dato cambie sin saber quién lo solicitó, quién lo aprobó y qué documento lo respaldó. Este flujo crea un hilo conductor inalterable.

Eficiencia Operativa: RRHH deja de ser un "digitador" de datos para convertirse en un "validador". El empleado asume la responsabilidad de la carga de información, reduciendo el cuello de botella administrativo.



**12. Workflow de Gestión Disciplinaria y Memorandos**

- **Propósito:** Registrar llamadas de atención, sanciones o memorandos de felicitación.
- **Justificación:** Provee la base legal para procesos de desvinculación justificada; sin este flujo, las sanciones no tendrían validez en un juicio laboral al no estar debidamente notificadas y firmadas.
### **Definición**
Este workflow orquesta el registro, notificación y archivo de eventos de conducta o desempeño (tanto positivos como negativos). Su objetivo es construir un **historial inalterable de evidencias** que sirva como respaldo legal ante posibles procesos de desvinculación con causa justificada (Art. 16 de la LGT) o para programas de incentivos, garantizando que el trabajador sea debidamente notificado.
### **2. Detalle del Flujo (Happy Path & Edge Cases)**
1. **Reporte de Incidente (MSS/HR):** Un supervisor o el área de RRHH inicia el reporte de una infracción (atraso reiterado, falta ética, incumplimiento de consignas) o un mérito (felicitación).
1. **Clasificación y Severidad:**
   1. **Leve:** Llamada de atención verbal (registro interno).
   1. **Grave:** Memorándum de amonestación escrita o Suspensión disciplinaria (sin goce de haberes, según reglamento interno).
   1. **Mérito:** Memorándum de felicitación.
1. **Carga de Evidencia:** El sistema exige adjuntar pruebas (fotos, registros de asistencia, testimonios o informes técnicos) que se vinculan al DigitalKardex.
1. **Generación del Memorándum:** El sistema genera el documento PDF inalterable basado en plantillas legales preconfiguradas para el Tenant.
1. **Notificación y Firma (ESS):**
   1. El trabajador recibe una notificación en su app/portal.
   1. **Firma Digital:** El trabajador firma el acuse de recibo.
   1. **Rechazo de Firma:** Si el trabajador se niega a firmar digitalmente, el sistema permite que RRHH registre la "Notificación por Testigos", subiendo el acta firmada físicamente como respaldo.
1. **Actualización del Expediente:** El documento se aloja en el DocumentRecord con estado Confirmed.
1. **Evaluación de Reincidencia (Regla de Negocio):**
   1. El sistema escanea el historial del WorkerProfile.
   1. **Invariante:** Si el trabajador acumula 3 memorandos por la misma falta en un periodo de tiempo determinado, el sistema dispara una alerta de **"Riesgo de Despido Justificado"** a Legal y RRHH

Justificación Técnica

Sustento Legal (Bolivia): En Santa Cruz, un despido por "incumplimiento de contrato" rara vez prospera en el Ministerio de Trabajo sin memorandos previos firmados. Este flujo garantiza la cadena de custodia de la prueba.

Inalterabilidad (WORM): Una vez que el memorándum es emitido y firmado, el sistema bloquea cualquier edición (Write Once, Read Many). Esto evita que RRHH "fabrique" memorandos con fechas pasadas durante un juicio laboral.

Análisis Predictivo: Al alimentar el dominio de AI Insights, el sistema puede predecir qué departamentos o sucursales de Retail tienen mayores conflictos laborales basados en la densidad de memorandos emitidos.



**13. Workflow de Suplencias y Reemplazos Temporales**

- **Propósito:** Gestionar la cobertura de un puesto por ausencia del titular.
- **Justificación:** Vital en **Universidades y Retail**; el sistema asigna temporalmente las funciones y recargos al suplente, manteniendo la plaza ocupada sin crear un contrato indefinido adicional.
### **Definición**
Este workflow gestiona la asignación transitoria de responsabilidades y autoridad de una Position (Plaza) cuyo titular se encuentra ausente (por vacaciones, baja médica o misión de campo). Su objetivo es garantizar la continuidad operativa en **Retail** y **Universidades** sin generar nuevos contratos indefinidos, habilitando el pago de recargos por suplencia y el acceso temporal a activos o permisos del titular.
### **2. Detalle del Flujo (Happy Path & Edge Cases)**
1. **Gatillo de Necesidad:**
   1. **Automático:** Se aprueba una ausencia de larga duración (Workflow 6).
   1. **Manual:** Un Gerente de Tienda o Decano solicita un suplente para cubrir una vacante crítica.
1. **Identificación del Suplente (Internal Search):**
   1. El sistema filtra candidatos internos según el SkillSet (Dominio 15) y disponibilidad de horario.
   1. **Invariante de Carga (P5):** El sistema bloquea al suplente si la suma de su jornada actual más la suplencia excede las **48h (varones) o 40h (mujeres)** semanales en Santa Cruz, a menos que se autoricen horas extra.
1. **Configuración de la Suplencia:**
   1. Se define el periodo (effective\_from / effective\_to).
   1. **Elegibilidad Salarial:** El sistema calcula el "Recargo por Suplencia" (diferencia salarial entre el cargo base del suplente y el cargo que reemplaza).
1. **Asignación Temporal (Secondary Assignment):**
   1. Se crea un registro de TemporaryAssignment vinculado a la plaza del titular.
   1. **Si es Educación:** El sistema vincula al suplente a los grupos/materias específicos en el AcademicProfile.
1. **Traspaso Temporal de Activos (Workflow 8):**
   1. Si la plaza requiere activos críticos (llaves de bóveda, credenciales de sistema), se dispara una "Asignación Temporal de Custodia".
1. **Ejecución y Marcación:**
   1. El suplente marca asistencia en la sucursal o aula de la suplencia. El sistema reconoce la ubicación mediante OrgUnit y geolocalización.
1. **Finalización y Retorno:**
   1. Al llegar la fecha fin o al reincorporarse el titular (RELATIONSHIP\_REACTIVATED), el sistema termina la asignación temporal.
   1. Se disparan las alertas para la devolución de activos temporales.
1. **Cierre en Nómina:** El sistema consolida las horas trabajadas como "Suplencia" para el pago del bono o recargo correspondiente en el mes

Justificación Técnica

Evita la Multi-Contratación: En lugar de crear un segundo contrato (que en Bolivia podría interpretarse como un vínculo indefinido paralelo), el sistema utiliza una Asignación Secundaria Temporal. Esto protege al Tenant ante demandas de duplicidad de beneficios.

Precisión de Costos (P&L): El costo del suplente (salario base + recargo) se imputa al CostCenter de la plaza que está cubriendo, manteniendo la rentabilidad de la sucursal o facultad exacta.

Cumplimiento de Jornada (Santa Cruz): La validación automática de las 40h/48h evita infracciones a la Ley General del Trabajo durante picos de suplencias en periodos de alta rotación (Retail).



**14. Workflow de Certificaciones y Constancias Digitales**

- **Propósito:** Emisión automática de certificados de trabajo y haberes con firma digital.
- **Justificación:** Descongestiona el área de RRHH; permite que el trabajador obtenga documentos para trámites bancarios o de salud (Caja) directamente desde el **ESS** con validación QR de autenticidad.
### **Definición**
Este workflow automatiza la generación, firma y entrega de documentos oficiales (certificados de trabajo, boletas de pago, constancias de haberes) a través del **ESS (Self-Service)**. Su objetivo es eliminar la carga operativa manual de RRHH en Santa Cruz, permitiendo que el colaborador obtenga documentos con validez legal inmediata para trámites bancarios, de salud (Caja de Salud) o personales, garantizando la autenticidad mediante firmas digitales y códigos QR.
### **2. Detalle del Flujo (Happy Path & Edge Cases)**
1. **Solicitud de Documento (ESS):** El empleado ingresa al portal y selecciona el tipo de certificado requerido (ej. "Certificado de Trabajo con Sueldo").
1. **Validación de Elegibilidad y Estado:**
   1. El sistema verifica que la Relationship esté **Activa** o **Suspendida** (no liquidada totalmente).
   1. **Invariante:** Si el empleado tiene una desvinculación en curso (OFFBOARDING\_INITIATED), el sistema bloquea la emisión automática y deriva la solicitud a un analista para revisión legal.
1. **Extracción Dinámica de Datos:**
   1. El motor consulta el PersonIdentity (Nombres, CI).
   1. Consulta el EmploymentAgreement (Cargo, Fecha de Ingreso).
   1. Consulta el historial de **Payroll** (para promedios salariales o últimos haberes percibidos).
1. **Generación de Documento y Estampado Digital:**
   1. Se aplica la plantilla (Template) correspondiente al Tenant y propósito.
   1. **Firma Digital:** El sistema invoca el certificado digital de la empresa para firmar el PDF (Hash de integridad).
   1. **Generación de QR:** Se estampa un código QR único que apunta a una URL de validación pública de **PeopleCoreIA**.
1. **Notificación y Disponibilidad:**
   1. Se dispara el evento CERTIFICATE\_GENERATED.
   1. El documento se aloja temporalmente en el DigitalKardex del empleado para descarga inmediata.
1. **Validación Externa (Consumo por Terceros):**
   1. Cuando un tercero (ej. un banco en Santa Cruz) escanea el QR, el sistema muestra una pantalla de confirmación con los datos esenciales para prevenir falsificaciones
### **Justificación Técnica**
- **Descongestión de RRHH:** En empresas de **Retail** con miles de empleados, la solicitud de certificados para créditos micro-financieros es masiva. Automatizar esto ahorra cientos de horas hombre al mes.
- **Seguridad y No Repudio:** El uso de firmas digitales basadas en estándares bolivianos y el QR de validación protege a la empresa contra la alteración de montos salariales en certificados físicos, un riesgo común en trámites fraudulentos.
- **Disponibilidad 24/7:** El empleado no depende del horario de oficina de RRHH. Esto mejora significativamente la experiencia del colaborador (EX) y la percepción de modernidad de la empresa.



**1. Contexto: Workforce & Org Master (Identidad y Estructura)**

**Este contexto notifica cambios en la "materia prima" del sistema: la persona y su ubicación.**

- **PERSON\_CREATED**
- **Gatillo y Naturaleza: Sincrónico. Se dispara manualmente desde el módulo de reclutamiento o vía API desde un ATS externo al registrar los datos básicos de un ciudadano.**
- **Lógica Funcional: Crea la raíz de identidad civil. Es el "ancla" que sobrevive a cualquier contrato. No genera un vínculo laboral aún, solo una entidad Person en estado Draft.**
- **UI e IA:**
- **UI: Formulario de alta rápida con validación de máscara para CI/Pasaporte.**
- **IA: El motor de *Data Quality* califica la completitud del perfil para predecir la viabilidad del proceso de contratación.**
- **Localización: El sistema debe permitir tipos de identidad bolivianos (CI con complemento) y extranjeros (Pasaporte).**
- **Invariantes: Garantiza la creación de un GlobalID único antes de proceder a cualquier validación de duplicidad.**
- **PERSON\_MASTER\_CREATED**
- **Gatillo y Naturaleza: Asincrónico. Se dispara tras la validación exitosa de la identidad contra fuentes oficiales o procesos internos de auditoría.**
- **Lógica Funcional: Eleva el estado de la persona a Master. Este evento notifica al Digital Kardex para que genere la estructura de carpetas necesaria para los documentos obligatorios.**
- **UI e IA:**
- **UI: El perfil del usuario muestra un check de "Identidad Verificada".**
- **IA: Identifica patrones demográficos (edad, ubicación) para sugerir beneficios o seguros de salud adecuados.**
- **Localización: Valida que el CI tenga el formato correcto según el departamento emisor en Bolivia.**
- **Invariantes: Protege la regla de Identidad Única: una persona física solo posee un registro maestro en el sistema.**
- **PERSON\_DEDUPLICATION\_MATCH\_FOUND**
- **Gatillo y Naturaleza: Sincrónico (Bloqueante). Se dispara automáticamente cuando el motor de reglas detecta un CI o pasaporte ya existente en la base de datos histórica.**
- **Lógica Funcional: Detiene el flujo de creación. Es crítico para el reingreso de personal, ya que permite recuperar la antigüedad acumulada para el Bono de Antigüedad e Indemnización.**
- **UI e IA:**
- **UI: Modal de bloqueo que muestra el perfil existente y solicita permiso para realizar un *Merge* o reactivación.**
- **IA: Realiza *Fuzzy Matching* en nombres y fechas de nacimiento para detectar duplicados incluso con errores de digitación en el CI.**
- **Invariantes: Evita registros duplicados que causarían pagos dobles de beneficios sociales o inconsistencias en la Gestora Pública.**
- **PERSON\_UPDATED**
- **Gatillo y Naturaleza: Asincrónico. Se dispara tras la edición de datos maestros (apellidos por matrimonio, cambio de domicilio o estado civil).**
- **Lógica Funcional: Notifica a los módulos de Payroll (para RC-IVA) y Digital Kardex (para solicitar nuevos documentos como el Certificado de Matrimonio).**
- **UI e IA:**
- **UI: Feed de auditoría visual indicando el "antes" y el "después".**
- **IA: Si el cambio de domicilio es a otro departamento (ej. de Santa Cruz a La Paz), la IA sugiere revisar las políticas de INFOCAL regional.**
- **Invariantes: Mantiene la integridad del historial (Effective Dating). El cambio no borra el dato anterior, crea una nueva versión con fecha de vigencia.**
- **RELATIONSHIP\_CREATED**
- **Gatillo y Naturaleza: Sincrónico. Disparado por la aprobación final del flujo de *Onboarding*.**
- **Lógica Funcional: Crea el vínculo legal entre la Person y el Tenant. Define si el perfil será WorkerProfile o AcademicProfile.**
- **UI e IA:**
- **UI: El estado del colaborador cambia a Onboarding o Active.**
- **IA: Predice el tiempo estimado de rampa (Onboarding time) basado en el tipo de tenant (Retail es más rápido que Educación).**
- **Invariantes: Valida la regla de No Traslape de Vínculos Primarios para evitar contingencias legales por doble percepción.**
- **RELATIONSHIP\_REACTIVATED**
- **Gatillo y Naturaleza: Sincrónico. Disparado en casos de reincorporación tras bajas largas o reingresos tras finiquitos previos.**
- **Lógica Funcional: Restablece la relación y dispara el recálculo de la línea de tiempo de antigüedad en el AccrualVault.**
- **Localización: En Bolivia, el sistema debe verificar si el reingreso ocurre en menos de 90 días para determinar si la antigüedad se mantiene ininterrumpida.**
- **Invariantes: Protege la continuidad laboral para el cálculo de vacaciones (P13) y quinquenios (P8).**
- **RELATIONSHIP\_ENDED**
- **Gatillo y Naturaleza: Sincrónico (Inicia el Offboarding). Disparado por renuncia, despido o fallecimiento.**
- **Lógica Funcional: Cierra el expediente y activa el cronómetro de 15 días calendario para el pago del Finiquito (P17). Notifica al módulo de Assets para la devolución de equipos.**
- **UI e IA:**
- **IA: Análisis de *Churn* para identificar si la salida se debe a factores de clima laboral o competitividad salarial.**
- **Invariantes: Libera la plaza en el Headcount Management y bloquea accesos al ESS.**

**ORG\_UNIT\_ASSIGNED\_CHANGED**

- **Gatillo y Naturaleza: Asincrónico. Disparado por una transferencia interna (movimiento entre sucursales o departamentos).**
- **Lógica Funcional: Actualiza la imputación analítica en Budget Control. El costo laboral se moverá de un centro de costos a otro.**
- **Localización: Si el cambio es entre tiendas de Retail en Santa Cruz, se prorratea el costo entre tiendas para el P&L mensual.**
- **Invariantes: Garantiza la regla de No Unidades Huérfanas: el colaborador siempre debe pertenecer a un nodo jerárquico.**

**ORG\_UNIT\_GEOGRAPHIC\_MOVED**

- **Gatillo y Naturaleza: Asincrónico. Disparado cuando una unidad administrativa física se traslada (ej. la sede central se muda de zona).**
- **Lógica Funcional: Actualiza las coordenadas para el módulo de marcación por geocerca (Time & Attendance).**
- **Localización: Afecta el cálculo de INFOCAL si la unidad sale del departamento de Santa Cruz (tasa del 1% deja de aplicar).**

**POSITION\_ASSIGNED**

- **Gatillo y Naturaleza: Sincrónico. Vincula oficialmente al trabajador con una plaza presupuestada.**
- **Lógica Funcional: Valida que la posición tenga presupuesto y que el sueldo pactado sea >= Bs 3.300 (SMN 2026).**
- **UI e IA:**
  - **UI: Actualización del organigrama en tiempo real.**
- **Invariantes: Control de Plazas (Headcount): impide asignar personal si no hay plazas vacantes autorizadas.**

**POSITION\_VACATED**

- **Gatillo y Naturaleza: Asincrónico. Disparado cuando una persona deja su puesto (promoción o retiro).**
- **Lógica Funcional: Notifica a Reclutamiento (ATS) que la plaza está disponible para ser publicada.**
- **UI e IA:**
  - **IA: Recomienda candidatos internos para sucesión basados en el Talent Inventory.**

**ACADEMIC\_PROFILE\_RANK\_UPDATED**

- **Gatillo y Naturaleza: Sincrónico (Tenant Educación). Disparado por la aprobación de un ascenso en el escalafón docente por méritos académicos.**
- **Lógica Funcional: Actualiza la categoría del docente (ej. de Auxiliar a Titular). Dispara automáticamente una adenda salarial por cambio de rango.**
- **UI e IA:**
  - **UI: Notificación de felicitación al docente en su ESS.**
- **Invariantes: Valida que el docente tenga los títulos académicos necesarios registrados y validados en el Kardex Digital antes de permitir el ascenso.**

**2. Contexto: Legal & Compliance (Cerebro Contractual)**

**Eventos que dictan las reglas de juego para Tiempo, Nómina y Auditoría Legal.**

**CONTRACT\_DRAFTED**

**Registro del objeto legal en estado latente.**

- **Gatillo y Naturaleza (Sync): Acción manual del Analista de RRHH o automatismo del módulo de Reclutamiento (ATS). Es Sincrónico para validar que la Position esté vacante antes de permitir el borrador.**
- **Lógica Funcional y Efectos Colaterales: Crea un Contract en estado Draft. Bloquea la Position para que no se asigne a otro candidato. El módulo de Budget Control recibe una reserva preventiva de fondos (Pre-encumbrance).**
- **UI e IA:**
  - **UI: Indicador de "Posición Reservada".**
  - **IA: El sistema analiza si el sueldo propuesto está alineado con la banda salarial (Grade/Band) del cargo para evitar inequidades internas.**
- **Localización: Se parametriza el tipo de contrato (Indefinido, Plazo Fijo, Obra). En ONGs, se exige el ID del Proyecto/Donante como metadato obligatorio.**
- **Impacto en Invariantes: Protege la "Control de Plazas": No se puede draftear un contrato si el *headcount* está al 100%.**

**CONTRACT\_APPROVED**

**Formalización del vínculo tras la Segregación de Funciones (SoD).**

- **Gatillo y Naturaleza (Async): Aprobación por un usuario de mayor jerarquía (Gerente de RRHH o Finanzas). Es Asincrónico para la cadena de notificaciones, pero Sincrónico para la persistencia del estado.**
- **Lógica Funcional: Cambia el estado a Approved. Genera el DocumentRecord en el Digital Kardex. Si la effective\_from es hoy, activa automáticamente la Relationship a Active.**
- **UI e IA:**
  - **UI: Check de validación verde. Generación automática del PDF con firma digital o QR de validación.**
  - **IA: Registra el tiempo de ciclo desde el borrador hasta la aprobación para KPIs de eficiencia de contratación.**
- **Diseño para Localización: Si el empleado está en Santa Cruz y el Tenant es Corporativo, se inyecta la regla del Aporte INFOCAL (1%) en el motor de nómina para este contrato.**
- **Impacto en Invariantes: Cumple con la "Segregación de Funciones": Un contrato no puede ser aprobado por la misma persona que lo drafteó.**

` `**CONTRACT\_LEGAL\_PISO\_VIOLATED**

**Salvaguarda contra la ilegalidad salarial.**

- **Gatillo y Naturaleza (Sync): Motor de reglas de cumplimiento (Compliance Engine). Es Sincrónico y Bloqueante.**
- **Lógica Funcional: Intercepta cualquier intento de guardar un salario menor al SMN vigente ($Bs. 3.300$). Detiene la transacción y registra un log de intento de violación normativa.**
- **UI e IA:**
  - **UI: Banner de error crítico: "Violación de Ley: El haber básico no puede ser inferior a Bs. 3.300".**
  - **IA: Genera un insight de riesgo legal para el Tenant, alertando sobre posibles multas administrativas.**
- **Diseño para Localización: El valor del SMN ($Bs. 3.300$) es una variable global versionada. Si el gobierno decreta un incremento, se actualiza la política y el evento reacciona al nuevo umbral.**
- **Impacto en Invariantes: Protege el "Piso Salarial Legal".**

**CONTRACT\_TÁCITA\_RECONDUCCIÓN\_RISK**

**Alerta preventiva de conversión de contrato.**

- **Gatillo y Naturaleza (Async): Proceso programado (Cron Job) que escanea contratos de Plazo Fijo.**
- **Lógica Funcional: Se dispara a los $T-90$ días de la fecha de vencimiento. Envía alertas al supervisor y a RRHH.**
- **UI e IA:**
  - **UI: Widget en el Dashboard: "Contratos con riesgo de reconducción".**
  - **IA: Predice el impacto financiero si el contrato se vuelve indefinido (Cálculo de provisión de indemnización por años de servicio).**
- **Diseño para Localización: Basado en el Art. 12 de la LGT boliviana. El sistema diferencia si es el primer o segundo contrato a plazo fijo.**
- **Impacto en Invariantes: Mantiene la "Integridad Temporal": Evita que el sistema mantenga contratos vencidos activos sin adenda.**

**MAX\_RENEWALS\_REACHED**

**Bloqueo de fraude a la ley por renovaciones sucesivas.**

- **Gatillo y Naturaleza (Sync): Motor de reglas al intentar crear una nueva adenda de prórroga. Sincrónico y Bloqueante.**
- **Lógica Funcional: Si el contrato ha llegado a 2 renovaciones de plazo fijo (límite en Bolivia), bloquea cualquier opción que no sea "Conversión a Indefinido" o "Terminación".**
- **UI e IA:**
  - **UI: Opción de "Extender Plazo Fijo" deshabilitada con tooltip explicativo sobre el límite legal.**
- **Localización: Implementación estricta de la jurisprudencia boliviana sobre fraude a la ley en contratos sucesivos.**
- **Impacto en Invariantes: Garantiza que la "Naturaleza del Vínculo" sea legalmente coherente.**

**ADENDUM\_APPROVAL\_REQUIRED**

**Control de cambios en las condiciones pactadas.**

- **Gatillo y Naturaleza (Sync): Modificación de salario, cargo o jornada en un contrato activo.**
- **Lógica Funcional: Crea una versión Pending de la adenda. Mantiene la versión actual del contrato como Active hasta que la adenda sea aprobada.**
- **UI e IA:**
  - **UI: Comparativa visual "Lado a Lado" (Antes vs. Después) para el aprobador.**
- **Diseño para Localización: Si el cambio es de Santa Cruz a otra ciudad, alerta sobre cambios en feriados regionales y aportes patronales específicos.**
- **Impacto en Invariantes: Soporta el "Effective Dating": Los cambios no sobreescriben, crean una nueva línea de tiempo.**

**ADDENDUM\_SALARY\_ADJUSTMENT\_APPROVED**

**Impacto financiero y recalibración de beneficios.**

- **Gatillo y Naturaleza (Async): Aprobación final de la adenda salarial.**
- **Lógica Funcional: Actualiza el haber básico. Notifica al módulo de Seniority & Benefits para recalcular el Bono de Antigüedad (especialmente si el SMN cambió).**
- **UI e IA:**
  - **IA: Calcula la "Deriva Salarial": ¿Cuánto aumentó el costo total de la planilla con este ajuste?**
- **Diseño para Localización: Recalcula el RC-IVA bajo la "Ley de Transparencia" (13% real) para mostrar al empleado su nuevo sueldo neto estimado.**
- **Impacto en Invariantes: Actualiza la "Base de Cálculo Inviolable" para futuras indemnizaciones.**

**CONTRACT\_TERMINATED**

**Cierre de obligaciones y activación de liquidación.**

- **Gatillo y Naturaleza (Sync/Async): Registro de baja (Renuncia, Despido, Fin de Contrato).**
- **Lógica Funcional: Cambia el estado a Terminated. Notifica a Social Security & Tax para la baja en la Gestora/Caja. Dispara el workflow de Finiquito con un cronómetro de 15 días.**
- **UI e IA:**
  - **UI: Checklist de "Offboarding" (Entrega de activos, firma de finiquito).**
  - **IA: Análisis de causa raíz de la baja (Entrevista de salida digital) para detectar fugas de talento.**
- **Diseño para Localización: Calcula automáticamente si corresponde Desahucio (3 salarios) en caso de despido injustificado según ley boliviana.**
- **Impacto en Invariantes: Libera la Position y desactiva la Relationship para evitar pagos en el siguiente ciclo de nómina.**

**3. Contexto: Dossier & Talent (Expediente y Activos)**

**Gestión de evidencias documentales y responsabilidad material.**

Digital Kardex & Compliance Documental

**DOCUMENT\_RECORDED**

- **Gatillo y Naturaleza (Async): Carga manual de un archivo vía ESS/MSS o integración con escáner. Es Asincrónico para permitir el procesamiento de *storage*, generación de miniaturas y cálculo de integridad.**
- **Lógica Funcional y Efectos: Genera una entrada en DocumentRecord. Se calcula un Hash (SHA-256) para garantizar la inalterabilidad ante auditorías del Ministerio de Trabajo.**
- **UI e IA:**
  - **UI: Barra de progreso y confirmación de "Documento en revisión".**
  - **IA: OCR para pre-llenar campos (ej: número de carnet, fecha de emisión) y detección de imágenes borrosas.**
- **Localización: El metadato debe incluir la "Regional" del documento (ej. CI emitido en Santa Cruz).**
- **Invariantes: Un documento no validado no puede activar una regla de elegibilidad.**

**DOCUMENT\_VALIDATION\_REJECTED**

- **Gatillo y Naturaleza (Sync): Acción manual del Analista de RRHH tras revisar la evidencia. Es Sincrónico para notificar al usuario de inmediato.**
- **Lógica Funcional: El estado cambia a Rejected. Dispara una notificación al ESS. Si el documento era "Crítico", puede disparar un ELIGIBILITY\_SUSPENDED.**
- **UI e IA:**
  - **UI: Modal de comentarios obligatorios para explicar el rechazo.**
  - **IA: Sugiere al usuario cómo corregir el documento basado en el motivo del rechazo.**
- **Invariantes: Mantiene el historial de versiones; el documento rechazado no se borra, se marca como inválido para auditoría.**

**DOCENT\_ACADEMIC\_TITLE\_VERIFIED**

- **Gatillo y Naturaleza (Sync): Validación técnica (títulos en Provisión Nacional). Es Sincrónico para asegurar invariantes de jerarquía.**
- **Lógica Funcional: Actualiza el AcademicProfile. Notifica al motor de Escalafón para habilitar posibles ascensos de categoría docente.**
- **UI e IA:**
  - **IA: Compara el título con el descriptor del cargo para detectar brechas de formación (*Skill Gap*).**
- **Localización: Específico para el Tenant Educación. Valida contra la base de datos de títulos reconocidos en Bolivia.**

**HEALTH\_CARD\_EXPIRATION\_WARNING**

- **Gatillo y Naturaleza (Async): Disparado por un *Worker* de monitoreo cronológico (T-30 días).**
- **Lógica Funcional: Notifica al colaborador y al supervisor. Si es Retail, el sistema marca la plaza como "En riesgo de inhabilitación".**
- **UI e IA:**
  - **IA: Predice el impacto operativo si 5 cajeros pierden su carnet sanitario simultáneamente en la misma sucursal.**
- **Localización: Crítico para Retail en Santa Cruz (Sedeges/Alcaldía).**

**MANDATORY\_COMPLIANCE\_DOC\_MISSING**

- **Gatillo y Naturaleza (Sync): Motor de reglas al intentar cambiar el estado de la relación a "Activo". Bloqueante.**
- **Lógica Funcional: Impide el cierre del Onboarding. El sistema no genera el alta en planilla hasta que la matriz de cumplimiento esté al 100%.**
- **Impacto en Invariantes: Bloqueo por Ilicitud: No se puede activar un cajero sin antecedentes policiales o un médico sin título verificado.**

**ELIGIBILITY\_SUSPENDED\_BY\_COMPLIANCE**

- **Gatillo y Naturaleza (Sync): Automático al expirar un documento crítico.**
- **Lógica Funcional: Interceptor que bloquea al empleado en el módulo de Scheduling (Turnos). No puede ser asignado a una grilla horaria.**
- **UI e IA:**
  - **UI: El nombre del empleado aparece en gris/rojo con el icono "Suspendido por Compliance".**
- **Invariantes: El colaborador sigue teniendo una Relationship activa (para efectos de antigüedad), pero su elegibilidad operativa es nula.**

**ELIGIBILITY\_RESTORED**

- **Gatillo y Naturaleza (Sync): Disparado tras la validación de un documento de reemplazo.**
- **Lógica Funcional: Libera los bloqueos en el módulo de Turnos y Nómina.**
- **UI e IA:**
  - **IA: Analiza el tiempo que el empleado estuvo "fuera de servicio" para KPIs de cumplimiento.**

Assets & Custodia de Bienes

**ASSET\_LOANED\_TO\_WORKER**

- **Gatillo y Naturaleza (Sync): Registro de entrega física. Requiere firma digital del empleado (OTP o Firma Simple).**
- **Lógica Funcional: Crea el vínculo AssignedAsset. El valor del activo se registra en el expediente del trabajador.**
- **UI e IA:**
  - **UI: Generación automática del Acta de Entrega en PDF.**
- **Invariantes: Un activo no puede ser prestado si ya figura como asignado a otra persona.**

**ASSET\_TRANSFER\_REQUIRED**

- **Gatillo y Naturaleza (Async): Reacción al evento ORG\_UNIT\_GEOGRAPHIC\_MOVED.**
- **Lógica Funcional: Alerta al área de Activos Fijos que el equipo asignado debe moverse físicamente entre sedes (ej. de Sede Norte a Sede Equipetrol).**
- **Localización: Específico para traslados territoriales en Santa Cruz.**

**ASSET\_RETURNED**

- **Gatillo y Naturaleza (Sync): Recepción de equipo por parte de Almacén/TI.**
- **Lógica Funcional: Libera la responsabilidad del trabajador. El activo vuelve al estado "Disponible".**
- **UI e IA:**
  - **IA: Evalúa el ciclo de vida del activo (desgaste) comparado con el tiempo de uso.**

**ASSET\_DAMAGE\_REPORTED**

- **Gatillo y Naturaleza (Sync): Registro de incidente. Puede ser disparado por el empleado (ESS) o supervisor.**
- **Lógica Funcional: Documenta el daño con evidencia fotográfica. Dispara un flujo de "Conciliación de Daño" para decidir si aplica descuento por planilla (previa revisión legal).**
- **Impacto en Invariantes: Si el daño es crítico, el activo se marca como "No Operativo" pero sigue bajo custodia hasta su baja técnica.**

**OFFBOARDING\_BLOCKED\_BY\_ASSETS**

- **Gatillo y Naturaleza (Sync): Guardrail Bloqueante en el cierre de la relación laboral.**
- **Lógica Funcional: El proceso de liquidación financiera (Finiquito) no puede avanzar a "Aprobado" si existen activos marcados como "En Custodia".**
- **UI e IA:**
  - **UI: Alerta roja en el panel de Offboarding: "Pendiente: 1 Laptop, 1 Celular".**
- **Impacto en Invariantes: Certificación de Devolución: Protege el patrimonio de la empresa antes de emitir el cheque de beneficios sociales.**

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

**5. Contexto: Financial & Social (Presupuesto y Egresos)**

**Relación con el dinero, fuentes de financiamiento y entes externos.**

FUNDING\_SOURCE\_VALIDATED

- **Gatillo y Naturaleza (Sync): Motor de validación presupuestaria al intentar aprobar un contrato o adenda. Es Sincrónico y bloqueante.**
- **Lógica Funcional y Efectos: El sistema consulta el agregado BudgetAllocation. Valida que el ProjectID o DonorID tenga saldo suficiente para cubrir el costo total cargado (Haber Básico + Cargas Patronales + Provisiones).**
- **UI e IA:**
- **UI: Indicador visual de "Presupuesto Comprometido" en la ficha de contratación.**
- **IA: Analiza la tasa de quema (*Burn Rate*) del proyecto para alertar si el presupuesto alcanzará hasta el final del contrato pactado.**
- **Diseño para Localización: Obligatorio para el Tenant ONG/Fundaciones. En otros Tenants, puede ser opcional o simplificado a un centro de costos.**
- **Impacto en Invariantes: Consistencia del 100%: No se puede validar si la fuente de financiamiento no cubre la totalidad de la asignación.**

  ` `**FUNDING\_SOURCE\_PROJECT\_EXHAUSTED**

- **Gatillo y Naturaleza (Async): Disparado por el proceso de cierre de nómina o por el motor de ejecución presupuestaria.**
- **Lógica Funcional: Alerta cuando una partida llega al 0% de disponibilidad. Puede disparar el flujo de CONTRACT\_TERMINATED o solicitar una transferencia de fondos.**
- **UI e IA:**
- **IA: Genera una proyección de "fecha de agotamiento" basada en los incrementos salariales y el Bono de Antigüedad vegetativo.**
- **Localización: Crítico para la gestión de proyectos de cooperación internacional en Bolivia.**

  **COST\_CENTER\_SPLIT\_ADJUSTED**

- **Gatillo y Naturaleza (Sync): Modificación manual o por transferencia de sede (ORG\_UNIT\_ASSIGNED\_CHANGED).**
- **Lógica Funcional: Actualiza el LaborCostSplit. El sistema debe recalcular la distribución del costo del empleado entre diferentes unidades.**
- **UI e IA:**
- **UI: Gráfico de torta que valida visualmente que la suma de los splits sea exactamente el 100%.**
- **Localización: Vital en Retail para prorratear el sueldo de un cajero que trabajó 15 días en la sucursal "Norte" y 15 días en la sucursal "Equipetrol".**
- **RC\_IVA\_FORM\_110\_IMPORTED**
- **Gatillo y Naturaleza (Async): Importación masiva de archivos. dec o integración vía API con el SIAT (SIAT en Línea).**
- **Lógica Funcional: Registra el crédito fiscal presentado por el empleado. Impacta directamente en el cálculo del Sueldo Neto en la planilla tributaria.**
- **UI e IA:**
- **IA: Detecta anomalías en los montos presentados comparados con el promedio histórico del empleado para prevenir errores de carga.**
- **Localización: Aplica el procedimiento de la "Ley de Transparencia":**

$$\text{Impuesto Determinado} = (\text{Sueldo Neto} - 2 \times \text{SMN}) \times 0.13$$Posteriormente resta el 13% de 1 SMN y el Formulario 110.

**BANK\_ACCOUNT\_SYNCED**

- **Gatillo y Naturaleza (Async): Reacción al evento DATA\_CHANGE\_REQUESTED una vez validado el documento bancario.**
- **Lógica Funcional: Actualiza el destino de la dispersión de fondos para el archivo de transferencia bancaria (BISA, BNB, Mercantil, etc.).**
- **UI e IA:**
  - **UI: Check de "Cuenta Validada para Abono".**
- **Invariantes: Bloquea la generación del archivo de pago si la cuenta bancaria no ha pasado por el evento de sincronización/validación.**

  **LIQUIDATION\_CALCULATED**

- **Gatillo y Naturaleza (Sync): Disparado al iniciar el proceso de Offboarding.**
- **Lógica Funcional: Genera el borrador del "Finiquito". El sistema promedia el Total Ganado de los últimos 90 días y calcula duodécimas de Aguinaldo e Indemnización.**
- **UI e IA:**
- **IA: Sugiere si el motivo de retiro (ej. despido sin causa) requiere la inclusión del Desahucio (3 salarios promedio).**
- **Localización: Basado estrictamente en la P15 y P17 del motor legal boliviano.**
- **Impacto en Invariantes: La liquidación debe estar vinculada a un CONTRACT\_TERMINATED.**

**FINIQUITO\_PAYMENT\_OVERDUE**

**Gatillo y Naturaleza (Async): Cronómetro legal que se activa en $T+16$ días calendario después del cese.**

**Lógica Funcional: Alerta Crítica. Notifica que el plazo de 15 días para el pago ha vencido.**

**UI e IA:**

**UI: Alerta roja persistente en el dashboard de Payroll y Legal.**

Impacto en Invariantes: Aplica la Multa del 30% sobre el saldo líquido pagable:$$\text{Total a Pagar} = \text{Saldo Liquidación} \times 1.30$$
Localización: Implementación de la sanción por mora en beneficios sociales (Bolivia 2026).

**6. Contexto: Intelligent Interaction (Experiencia y Procesos Orquestados)**

**Eventos que cierran procesos de usuario o disparan interacciones inteligentes.**

**ONBOARDING\_COMPLETED**

- **Gatillo y Naturaleza (Async): Se dispara automáticamente cuando el motor de estados confirma que los 4 pilares están validados: Identidad (Persona), Vínculo (Relationship), Reglas (Contract) y Ubicación (Position).**
- **Lógica Funcional y Efectos: Activa la elegibilidad en el motor de Payroll y Scheduling. Notifica al módulo de Assets para la entrega de equipos.**
- **UI e IA:**
  - **UI: Confeti visual en el ESS y habilitación del carnet digital.**
  - **IA: Genera un "Perfil de Riesgo de Deserción Temprana" comparando el tiempo de onboarding con la media del tenant.**
- **Invariantes: Impide que un empleado reciba sueldo sin tener un contrato firmado y una posición asignada.**

**OFFBOARDING\_INITIATED**

- **Gatillo y Naturaleza (Sync): Registro manual de la baja. Es Sincrónico para bloquear inmediatamente la creación de nuevos turnos en el futuro.**
- **Lógica Funcional: Dispara el cálculo de la P15 (Promedio 90 días) y P17 (Finiquito). Notifica a TI para la suspensión de accesos en la effective\_to.**
- **UI e IA:**
  - **IA: Análisis de sentimiento en la causa de baja para detectar problemas de liderazgo en unidades específicas.**
- **Impacto en Invariantes: Bloquea cualquier intento de pago de "Bono de Antigüedad" posterior a la fecha de cese.**

**DATA\_CHANGE\_REQUESTED / REJECTED**

- **Gatillo y Naturaleza (Async): Solicitud desde el ESS por el empleado.**
- **Lógica Funcional: Los cambios no impactan PersonMaster hasta que RRHH valide el respaldo (ej. Certificado de matrimonio para cambio de apellido).**
- **UI e IA:**
  - **UI: Línea de tiempo que muestra el estado "Pendiente de validación".**
- **Invariantes: Garantiza la Inalterabilidad de Auditoría: no se puede cambiar un dato civil sin una evidencia documental vinculada.**

**DISCIPLINARY\_ACTION\_REPORTED / MEMORANDUM\_ISSUED**

- **Gatillo y Naturaleza (Sync): Reporte de falta por un supervisor (MSS).**
- **Lógica Funcional: Genera una entrada en el Digital Kardex. Si es MEMORANDUM\_ISSUED, se crea el PDF inalterable con Hash SHA-256.**
- **Localización: En Bolivia, este evento es la base legal para el despido justificado (Art. 16 LGT). El sistema debe clasificar si la falta es "Grave" según el Reglamento Interno del Tenant.**
- **Impacto en Invariantes: Ningún memorándum puede eliminarse una vez emitido; solo puede anularse con una nota de rectificación.**

**MEMORANDUM\_ACKNOWLEDGED**

- **Gatillo y Naturaleza (Sync): Firma digital o acuse de recibo del empleado en el ESS.**
- **Lógica Funcional: Cierra el ciclo de notificación legal. Si el empleado rechaza la firma, el sistema habilita el flujo de "Notificación por Testigos".**
- **UI e IA:**
  - **IA: Identifica si el empleado tiene un patrón de "Faltas próximas a feriados de Santa Cruz" (ej. 24 de septiembre).**

**DISCIPLINARY\_THRESHOLD\_REACHED**

- **Gatillo y Naturaleza (Async): Motor de reglas de cumplimiento.**
- **Lógica Funcional: Alerta automática cuando un empleado acumula (ej.) 3 memorandos por la misma causa en 6 meses.**
- **UI e IA:**
  - **UI: Alerta roja al Gerente de RRHH: "Riesgo Legal: Posibilidad de Despido Justificado".**
- **Impacto en Invariantes: Protege la regla de "Proporcionalidad": Alerta si se intenta despedir sin el historial documental necesario.**

**SUBSTITUTION\_INITIATED / COMPLETED**

- **Gatillo y Naturaleza (Sync): Asignación temporal de una plaza por ausencia del titular.**
- **Lógica Funcional: Crea una TemporaryAssignment. Calcula el "Recargo por Suplencia" (Diferencia salarial).**
- **Diseño para Localización: En Universidades, este evento gestiona el "Docente Reemplazante" por materia, vinculando el AcademicProfile temporalmente al grupo.**
- **Invariantes: Evita que el suplente adquiera derechos de "Plazo Indefinido" en la plaza si la suplencia excede los límites de la política del tenant.**

**CERTIFICATE\_REQUESTED / GENERATED / VALIDATED\_EXTERNALLY**

- **Gatillo y Naturaleza (Async): Solicitud autónoma del empleado.**
- **Lógica Funcional: El sistema extrae datos de Person, Contract y Payroll para generar el PDF. El QR apunta a una URL de validación pública (Zero-Trust).**
- **UI e IA:**
  - **UI: Descarga inmediata en PDF.**
  - **IA: Detecta si hay una alta demanda de certificados para "Entidades Bancarias", sugiriendo una tendencia de sobre-endeudamiento en la plantilla.**
- **Invariantes: Solo se generan certificados si el empleado no tiene bloqueos por documentos vencidos en el Digital Kardex.**

Roles dentro de RRHH:

**Roles Globales (Presentes en todos los Tenants)**

**1. Super Admin (Soporte Técnico)**

- **Módulos con Acceso:** Lectura total en los 6 Bounded Contexts (Workforce, Legal, Dossier, Accruals, Financial, Experience).
- **Partes Específicas:** Visualización de registros maestros, configuración de sistema y colas de eventos.
- **Restricciones (Lo que NO ve/hace):** \* **Datos Sensibles:** Los campos de SalaryTerms y net\_salary\_proj aparecen enmascarados o cifrados.
  - **Acciones Prohibidas:** No tiene permisos de escritura ni puede eliminar registros del AuditLog o eventos de dominio.
- **Lógica ABAC:** Acceso global pero bloqueado por la invariante de inmutabilidad de logs.

**2. Administrador General / HR Super User**

- **Módulos con Acceso:** Escritura y configuración total en Workforce, Legal, Dossier y Accruals.
- **Partes Específicas:** Onboarding masivo, parametrización de políticas de bonos, ajuste de calendarios y gestión de OrgUnit.
- **Restricciones (Lo que NO ve/hace):** \* **Finanzas:** No puede conectarse a las APIs de banca para la dispersión final de fondos (reservado para Finanzas/Nómina).
  - **Seguridad:** No puede modificar los parámetros legales globales (SMN o UFV).
- **Diferencia por Tenant:** En **ONG**, se le exige asociar un FundingSource para crear plazas. En **Educación**, gestiona el AcademicProfile.

**3. Analista de Planillas (Payroll Specialist)**

- **Módulos con Acceso:** Acceso total a Financial & Social y Accruals & Time-Off .
- **Partes Específicas:** SalaryTerms, TaxForm110, promedios de finiquitos, aportes a la Gestora Pública y validación de RC-IVA.
- **Permiso Especial:** Puede ver la fecha de caducidad de documentos críticos (Carnet Sanitario/Títulos) en el DigitalKardex para validar suspensiones.
- **Gobernanza:** Es el único que valida el motivo de "Despido sin Causa" para activar el pago del Desahucio.
- **Restricciones:** No tiene acceso a expedientes clínicos o documentos cualitativos de desempeño que no afecten la nómina.

**4. Dependiente Final (Autoservicio - ESS)**

- **Módulos con Acceso:** Capa de Experience & IA (limitada a su perfil).
- **Partes Específicas:** Consulta de salarios pasados, descarga de boletas PDF, carga de facturas (Form. 110) y solicitud de vacaciones.
- **Restricciones:** Cifrado punto a punto; no puede ver datos de ningún otro empleado ni estructuras jerárquicas superiores

Roles por Tenant:

**1. Tenant: Retail (Comercial)**

En este sector, la seguridad se enfoca en la continuidad operativa de las tiendas y la protección de activos.

- **Gerente de Tienda (Store Manager)**
  - **Módulos con Acceso:**
    - **Workforce & Org Master**: Consulta de la estructura de su sucursal y datos básicos del personal asignado.
    - **Accruals & Time-Off**: Gestión de marcaciones, retrasos y justificación de ausencias.
    - **Dossier & Talent**: Registro de daños o entrega de activos (AssignedAsset).
    - **Experience & IA**: Configuración de turnos en la grilla de Scheduling.
  - **Partes Específicas:** Autorización de sobretiempos, visibilidad de cargos como "Cajero" o "Repositor" y monitoreo de carnet sanitario.
  - **Lo que NO se le muestra:** Acceso bloqueado a SalaryTerms (Haber Básico), finiquitos y aportes tributarios.
  - **Lógica ABAC:** Posee autoridad recursiva solo sobre su nodo geográfico. Hereda visibilidad temporal sobre empleados de otras sedes solo durante los días de transferencia efectiva .

**2. Tenant: ONG (Fundaciones)**

La seguridad aquí es analítica y se centra en el cumplimiento de los presupuestos de los donantes.

- **Coordinador de Proyecto**
  - **Módulos con Acceso:**
    - **Financial & Social**: Consulta de FundingSource y LaborCostSplit de sus proyectos asignados .
    - **Workforce & Org Master**: Visualización de la relación laboral vinculada al proyecto.
    - **Accruals & Time-Off**: Validación de hojas de tiempo (Timesheets) de misiones de campo.
  - **Partes Específicas:** Verificación de viáticos, asignación de descansos compensatorios y cumplimiento de objetivos de impacto social .
  - **Lo que NO se le muestra:** No puede ver la planilla global de la organización ni los sueldos de otros coordinadores de su mismo nivel.
- **Revisor de Fondos (Cumplimiento)**
  - **Módulos con Acceso:**
    - **Legal & Compliance**: Auditoría de contratos y adendas.
    - **Financial & Social**: Auditoría de ejecución presupuestaria.
  - **Partes Específicas:** Anexo de comentarios obligatorios en procesos de auditoría y generación de alertas por riesgo de "Tácita Reconducción" .
  - **Acción de Escritura:** Tiene permiso para suspender el procesamiento de una hoja de tiempo si detecta discrepancias financieras con el donante.

**3. Tenant: Educación (Universidades)**

El enfoque está en el escalafón académico y la separación de roles docentes y administrativos.

- **Decano**
  - **Módulos con Acceso:**
    - **Workforce & Org Master**: Gestión del AcademicProfile de su facultad.
    - **Dossier & Talent**: Validación de títulos de postgrado y méritos académicos.
    - **Experience & IA**: Aprobación de ascensos de categoría docente.
  - **Partes Específicas:** Escalafón docente, registros de investigaciones y carga horaria por materia.
  - **Lo que NO se le muestra:** Bloqueo absoluto a datos bancarios, deducciones de ley y expedientes del personal administrativo (WorkerProfile) .
- **Docente (Perfil Especializado ESS)**
  - **Módulos con Acceso:**
    - **Experience & IA**: Portal de autoservicio académico.
    - **Workforce & Org Master**: Consulta de su propio AcademicProfile.
  - **Partes Específicas:** Carga de certificados para ascenso de rango, visualización de materias asignadas y solicitud de vacaciones durante el "Receso Académico".
  - **Lo que NO se le muestra:** No puede ver el desempeño ni los expedientes de otros docentes de su facultad.

**4. Tenant: Corporativo (Privada)**

La estructura es piramidal y se basa en la planificación de carrera y el desempeño.

- **Jefe de Área / director**
  - **Módulos con Acceso:**
    - **Dossier & Talent**: Revisión de PerformanceSnapshot y planes de sucesión.
    - **Accruals & Time-Off**: Aprobación de vacaciones individuales (Escala 15/20/30 días).
    - **Experience & IA**: Notificaciones de méritos o memorandos disciplinarios.
  - **Partes Específicas:** Gestión cualitativa de objetivos y validación de inventario de activos tecnológicos asignados a su equipo.
  - **Lo que NO se le muestra:** Encriptación total de los salarios de sus subordinados y de otros jefes de su mismo nivel.

Atributos para ABAC:

|**Atributo**|**Fuente**|**Propósito**|
| :- | :- | :- |
|User.TenantID|Sesión|Garantiza el aislamiento absoluto entre empresas.|
|User.OrgUnitPath|Estructura|Permite la **Autoridad Recursiva** (ver hacia abajo en el árbol) .|
|User.Location|Perfil|Determina si el usuario puede gestionar políticas de Santa Cruz (Escenario A).|
|User.Context|Sesión|Separa el rol de "Docente" del de "Administrativo" .|
|Resource.CreatorID|Recurso|Implementa la **Segregación de Funciones (SoD)** (Creador $\neq$ Aprobador).|
|Resource.IsSensitive|Dominio|Protege datos de sueldos y memorandos de accesos no autorizados.|



Diccionario de datos:

Contexto 1: Workforce & Org Master (El Núcleo Operativo)

Aggregate 1:

|**Entidad / VO**|**Campo**|**Tipo de Dato**|**Restricciones / Lógica**|
| :- | :- | :- | :- |
|**Person** (Root)|person\_id|UUID|PK. Identificador global único.|
||first\_name|VARCHAR(100)|Obligatorio.|
||last\_name|VARCHAR(100)|Obligatorio.|
||birth\_date|DATE|**Invariante**: Debe ser $\ge 18$ años al momento del registro.|
||gender|ENUM|Varón, Mujer (Impacta en políticas de jornada).|
||global\_id|VARCHAR(50)|Único. Registro maestro inalterable.|
|**PartyIdentifier** (Entity)|identifier\_id|UUID|PK.|
||person\_id|UUID|FK a Person.|
||id\_type|ENUM|CI, Pasaporte.|
||id\_number|VARCHAR(30)|Alfanumérico único. Incluye complemento si aplica.|
||extension|ENUM|Departamental (SC, LP, etc.).|
||issue\_date|DATE|Fecha de emisión.|
||expiry\_date|DATE|Para control de vigencia documental.|
|**ContactPoint** (VO)|email|VARCHAR(150)|Reemplazable (Value Object).|
||phone|VARCHAR(20)|Reemplazable.|
||address|TEXT|Domicilio civil.|

Aggregate 2:

|**Entidad / VO**|**Campo**|**Tipo de Dato**|**Restricciones / Lógica**|
| :- | :- | :- | :- |
|**Relationship** (Root)|relationship\_id|UUID|PK. Soporta el "Multi-vínculo".|
||person\_id|UUID|FK a Person.|
||tenant\_id|UUID|Identificador de la empresa/institución.|
||rel\_type|ENUM|Laboral, Académico, Pasantía.|
||current\_status|ENUM|Draft, Active, Suspended, Terminated.|
||hire\_date|DATE|Fecha oficial de ingreso.|
|**WorkerProfile** (Entity)|profile\_id|UUID|PK. Atributos para roles operativos.|
||employee\_no|VARCHAR(20)|Número de legajo interno.|
|**AcademicProfile** (Entity)|academic\_id|UUID|PK. Exclusivo para Tenant Educación.|
||current\_rank|ENUM|Auxiliar, Adjunto, Titular, etc..|
||teaching\_load|INT|Límite de carga horaria semestral.|
|**StatusLog** (Entity)|log\_id|UUID|PK. Trazabilidad inalterable de estados.|
||previous\_status|ENUM|Estado anterior.|
||new\_status|ENUM|Estado nuevo.|
||change\_reason|TEXT|Justificación del cambio.|

Agreggate 3:

|**Entidad / VO**|**Campo**|**Tipo de Dato**|**Restricciones / Lógica**|
| :- | :- | :- | :- |
|**OrgUnit** (Root)|unit\_id|UUID|PK. Nodo de la jerarquía (Sucursal, Facultad).|
||parent\_id|UUID|FK a unit\_id. **Estructura de Árbol Único**.|
||name|VARCHAR(150)|Nombre de la unidad.|
||unit\_type|ENUM|Administrativa, Académica, Comercial.|
||geo\_coords|POINT/TEXT|Para marcación por geocerca en Santa Cruz.|
|**OrgHierarchy** (Entity)|hierarchy\_id|UUID|PK. Versionamiento de la estructura.|
||effective\_date|DATE|Fecha desde la cual es válida la estructura.|
|**CostCenter** (VO)|cost\_code|VARCHAR(50)|Etiqueta contable inmutable.|

Aggregate 4:

|**Entidad / VO**|**Campo**|**Tipo de Dato**|**Restricciones / Lógica**|
| :- | :- | :- | :- |
|**Position** (Root)|position\_id|UUID|PK. La "silla" en la organización.|
||unit\_id|UUID|FK a OrgUnit. Ubicación física/lógica.|
||pos\_status|ENUM|Vacant, Occupied, Reserved.|
||is\_budgeted|BOOLEAN|Control de respaldo financiero.|
|**Job** (VO)|job\_code|VARCHAR(20)|Descriptor de cargo estandarizado.|
||title|VARCHAR(100)|Título del cargo (ej. Cajero, Decano).|
||grade\_band|VARCHAR(10)|Banda salarial asociada.|
|**HeadcountPlan** (VO)|max\_slots|INT|**Invariante**: No se puede exceder el límite.|
||current\_slots|INT|Contador de ocupación actual.|


Contexto 2: Legal & Compliance (Cerebro Normativo)

Aggregate 1:

|**Entidad / VO**|**Campo**|**Tipo de Dato**|**Restricciones / Lógica**|
| :- | :- | :- | :- |
|**Contract** (Root)|contract\_id|UUID|PK. Vinculado a una Relationship.|
||relationship\_id|UUID|FK. Define a quién pertenece el contrato.|
||contract\_type|ENUM|Indefinido, Plazo Fijo, Obra/Servicio.|
||status|ENUM|Draft, Approved, Terminated.|
||project\_id|VARCHAR(50)|**Obligatorio para ONGs** (FundingSource ID).|
|**ContractAddendum** (Entity)|addendum\_id|UUID|PK. Identidad propia para versionado .|
||contract\_id|UUID|FK. Los cambios no sobreescriben, se adicionan.|
||effective\_from|DATE|Fecha de inicio de vigencia de la modificación.|
||effective\_to|DATE|Fecha fin (nulo si es indefinido).|
|**SalaryTerms** (VO)|basic\_salary|DECIMAL(15,2)|**Invariante**: $\ge$ Bs 3.300 (SMN 2026).|
||total\_earned\_proj|DECIMAL(15,2)|Proyección de Total Ganado (incluye bonos).|
||net\_salary\_proj|DECIMAL(15,2)|Proyección neta tras descuentos de ley.|
||currency|VARCHAR(3)|ISO (BOB por defecto).|
|**ComplianceSnapshot** (VO)|smn\_applied|DECIMAL(15,2)|Captura el SMN vigente al momento de la firma.|
||tax\_regime|VARCHAR(50)|Ley de Transparencia (RC-IVA 13%).|
||infocal\_active|BOOLEAN|Switch según la OrgUnit (1% en Santa Cruz).|

Aggregate 2:

|**Entidad / VO**|**Campo**|**Tipo de Dato**|**Restricciones / Lógica**|
| :- | :- | :- | :- |
|**PolicyRule** (Root)|policy\_id|UUID|PK. Identificador de la regla legal.|
||policy\_name|VARCHAR(100)|Ej: "Validación Piso Salarial", "Límite Renovación".|
||description|TEXT|Explicación legal (Art. LGT relacionado).|
|**LegalThreshold** (VO)|threshold\_value|DECIMAL(15,4)|Valores inmutables (3.300, 12.71, 0.13).|
||effective\_date|DATE|Fecha desde que rige este valor gubernamental.|
|**EligibilityMatrix** (VO)|matrix\_id|UUID|PK. Orquesta términos por perfil de empleado.|
||tenant\_type|ENUM|Retail, ONG, Corporativo, Educación.|
||job\_code|VARCHAR(20)|FK a Job. Filtra reglas por cargo.|
||req\_documents|JSONB|Lista de documentos obligatorios (P7).|






Entidades:

|**Entidad**|**Campo**|**Tipo de Dato**|**Restricciones / Lógica**|
| :- | :- | :- | :- |
|**AuditLog**|audit\_id|UUID|PK. Registro tipo WORM (Write Once, Read Many).|
||entity\_name|VARCHAR(50)|Nombre de la entidad afectada (ej: Contract).|
||diff\_data|JSONB|Almacena el "antes" y el "después" del cambio.|
||timestamp|TIMESTAMP|Fecha y hora exacta del servidor.|
|**WorkflowInstance**|workflow\_id|UUID|PK. Instancia de aprobación (Onboarding, Adenda).|
||creator\_user\_id|UUID|Usuario que inició la propuesta.|
||approver\_user\_id|UUID|**Invariante SoD**: Debe ser $\neq$ creator\_user\_id.|
|**ElectronicEvidence**|evidence\_id|UUID|PK. Vínculo con el documento legal firmado.|
||hash\_sha256|VARCHAR(64)|**Obligatorio para RAG**: Generado al cargar.|
||qr\_code\_url|VARCHAR(255)|URL de validación Zero-Trust.|

Contexto 3: Dossier & Talent (Memoria Institucional)

Aggregate 1:

|**Entidad / VO**|**Campo**|**Tipo de Dato**|**Restricciones / Lógica**|
| :- | :- | :- | :- |
|**DocumentRecord** (Root)|doc\_id|UUID|PK. Identificador único del documento.|
||relationship\_id|UUID|FK. Vincula el documento al vínculo laboral específico.|
||doc\_category|ENUM|Identidad, Salud, Académico, Disciplinario, Legal.|
||doc\_type|VARCHAR(50)|Ej: "Carnet Sanitario", "Título Provisión Nacional" .|
||is\_critical|BOOLEAN|**Invariante**: Si es TRUE, bloquea la elegibilidad operativa si expira .|
|**ValidationStatus** (VO)|current\_state|ENUM|Pending, Approved, Rejected, Expired .|
||reviewer\_id|UUID|Usuario de RRHH que validó la evidencia.|
||review\_date|TIMESTAMP|Fecha de la última validación.|
||reject\_reason|TEXT|Justificación obligatoria en caso de rechazo.|
|**DocumentMetadata** (VO)|storage\_id|UUID|**Storage ID**: Puntero al archivo en el servicio de almacenamiento.|
||file\_name|VARCHAR(255)|Nombre original del archivo cargado.|
||hash\_sha256|VARCHAR(64)|**Invariante RAG**: Generado al cargar para asegurar inalterabilidad.|
||expiry\_date|DATE|Fecha de caducidad (Gatilla alertas T-30).|

Aggregate 2:

|**Entidad / VO**|**Campo**|**Tipo de Dato**|**Restricciones / Lógica**|
| :- | :- | :- | :- |
|**AssignedAsset** (Root)|assignment\_id|UUID|PK. Registro de responsabilidad de custodia.|
||worker\_id|UUID|FK a WorkerProfile. Responsable del activo.|
||asset\_tag|VARCHAR(50)|Código de inventario único de la empresa.|
||status|ENUM|Bajo Custodia, Devuelto, Dañado, Extraviado .|
||assigned\_at|TIMESTAMP|Fecha de firma del acta de entrega digital.|
||returned\_at|TIMESTAMP|Fecha de recepción física y liberación.|
|**AssetDescriptor** (VO)|category|ENUM|Computación, Vehículo, Comunicación, Herramientas.|
||tech\_specs|JSONB|Características técnicas (ej. Marca, Serial, Modelo).|
||initial\_state|TEXT|Estado físico al momento de la entrega (ej: "Nuevo").|

Entidades:

|**Entidad**|**Campo**|**Tipo de Dato**|**Restricciones / Lógica**|
| :- | :- | :- | :- |
|**PerformanceSnapshot**|snapshot\_id|UUID|PK. Captura cualitativa de desempeño en un momento dado.|
||eval\_period|VARCHAR(20)|Gestión evaluada (ej: "Q1-2026").|
||score|DECIMAL(5,2)|Calificación numérica.|
|**SkillSet**|skill\_id|UUID|PK. Catálogo de competencias técnicas y blandas.|
||skill\_name|VARCHAR(100)|Ej: "Java Spring Boot", "Manejo de Valores".|
||proficiency|ENUM|Básico, Intermedio, Avanzado, Experto.|
|**TrainingHistory**|training\_id|UUID|PK. Registro de capacitaciones y certificaciones.|
||course\_name|VARCHAR(200)|Título de la formación.|
||doc\_id|UUID|FK a DigitalKardex. Vínculo con el certificado validado.|




Contexto 4: Accruals & Time-Off (Reloj de Beneficios)

Aggregate 1:

|**Entidad / VO**|**Campo**|**Tipo de Dato**|**Restricciones / Lógica**|
| :- | :- | :- | :- |
|**AccrualBalance** (Root)|balance\_id|UUID|PK. Identificador del saldo. |
||relationship\_id|UUID|FK. Vinculado al vínculo laboral. |
||balance\_type|ENUM|VACATION, SENIORITY\_BONUS, QUINQUENIO.|
||unit|ENUM|DAYS, AMOUNT\_BOB.|
||current\_balance|DECIMAL (15,2)|**Invariante**: No saldo negativo forzado. |
||last\_accrual\_date|DATE|Fecha del último cálculo automático.|
|**LeaveTransaction** (Entity)|transaction\_id|UUID|PK. Cada solicitud de ausencia. |
||balance\_id|UUID|FK. Saldo afectado. |
||start\_date|DATE|Fecha inicio de la ausencia. |
|||DATE|Fecha fin de la ausencia. |
||days\_requested|DECIMAL(5,2)|Días netos a descontar (excluye feriados). |
||status|ENUM|PENDING, APPROVED, REJECTED. |
|**SeniorityMilestone** (VO)|milestone\_id|UUID|PK. Evento de hito temporal. |
||months\_completed|INT|Meses de antigüedad (ej: 24, 60, 120). |
||base\_smn\_type|ENUM|BASE\_1\_SMN (ONG/Univ), BASE\_3\_SMN (Privada). |

Entidades:

|**Entidad**|**Campo**|**Tipo de Dato**|**Restricciones / Lógica**|
| :- | :- | :- | :- |
|**HolidayCalendar**|holiday\_id|UUID|PK. Registro de días no hábiles. |
||holiday\_date|DATE|Fecha del feriado. |
||scope|ENUM|NATIONAL, REGIONAL\_SCZ (24 Sep). |
|**QuinquenioProvision**|provision\_id|UUID|PK. Reserva contable mensual (8.33%). |
||relationship\_id|UUID|FK.|
||total\_accumulated|DECIMAL (15,2)|Monto proyectado para el pago de 5 años. |
||penalty\_active|BOOLEAN|**Invariante**: True si el pago excede 30 días. |
|**BenefitAccrual**|benefit\_id|UUID|PK. Acumulación de beneficios de ley. |
||benefit\_type|ENUM|AGUINALDO, PRIMA\_UTILIDAD. |
||fiscal\_year|INT|Gestión fiscal (ej: 2026). |
||accrued\_amount|DECIMAL (15,2)|Monto acumulado por duodécimas. |



Seniority\_Scale: Tabla dinámica para actualizar políticas sin alterar código, según lo acordado.

|**Campo**|**Tipo de Dato**|**Descripción / Lógica**|
| :- | :- | :- |
|scale\_id|UUID|PK.|
|min\_years|INT|Límite inferior de años (ej: 2). |
|max\_years|INT|Límite superior de años (ej: 5). |
|vacation\_days|INT|Días de escala: 15, 20 o 30. |
|bonus\_percentage|DECIMAL(5,2)|Porcentaje de Bono Antigüedad (5%, 11%, etc.). |

Contexto 5: Financial & Social (Egresos y Retenciones)

Aggregate:

|**Entidad / VO**|**Campo**|**Tipo de Dato**|**Restricciones / Lógica**|
| :- | :- | :- | :- |
|**FundingSource** (Root)|source\_id|UUID|PK. Identificador de la fuente (Donante X, Proyecto Y).|
||project\_code|VARCHAR(50)|**Obligatorio para ONGs**. Código único del proyecto.|
||total\_budget|DECIMAL(18,2)|Presupuesto total asignado a la partida.|
||available\_budget|DECIMAL(18,2)|Saldo actual. Gatilla FUNDING\_SOURCE\_PROJECT\_EXHAUSTED si llega a 0 .|
||burn\_rate|DECIMAL(5,2)|IA: Tasa de consumo proyectada para alertas de agotamiento.|
|**LaborCostSplit** (VO)|split\_id|UUID|PK. Registro de distribución.|
||unit\_id|UUID|FK a OrgUnit. Unidad que absorbe el costo.|
||percentage|DECIMAL(5,2)|**Invariante**: La suma de los splits para una relación debe ser exactamente **100%**.|
||effective\_date|DATE|Soporta cambios de imputación a mitad de mes.|


Entidades:

|**Entidad**|**Campo**|**Tipo de Dato**|**Restricciones / Lógica**|
| :- | :- | :- | :- |
|**SocialSecurityAccount**|ssa\_id|UUID|PK. Cuenta del trabajador en la Gestora Pública.|
||gestora\_code|VARCHAR(30)|Código único de asegurado (NUA/CUA).|
||contribution\_rate|DECIMAL(5,4)|**Fijo 2026**: **12,71%** sobre el Total Ganado.|
||last\_contribution|DATE|Fecha del último aporte reportado.|
|**HealthProvider**|provider\_id|UUID|PK. Ente gestor de salud (ej: Caja Nacional, Caja Petrolera).|
||registration\_no|VARCHAR(50)|Número de afiliación patronal/laboral.|
||status|ENUM|Activo, En trámite, Suspendido (Impacta en elegibilidad).|
|**TaxForm110**|form\_id|UUID|PK. Registro de presentación de facturas para RC-IVA .|
||total\_declared|DECIMAL(15,2)|Monto total de facturas presentadas en el mes.|
||verified\_credit|DECIMAL(15,2)|Crédito fiscal validado (13% del declarado).|
||doc\_id|UUID|FK a DigitalKardex. Respaldo con Hash SHA-256.|

Contexto 6: Experience & IA (Capa Inteligente)

Aggregate:

|**Entidad / VO**|**Campo**|**Tipo de Dato**|**Restricciones / Lógica**|
| :- | :- | :- | :- |
|**PredictionModel** (Root)|model\_id|UUID|PK. Identificador del modelo de IA (RAG/Analítica).|
||model\_type|ENUM|CHURN, LIABILITY\_RISK (Quinquenios), COMPLIANCE\_BREACH.|
||version|VARCHAR(20)|Control de versión del modelo (ej: "Veo-1.2").|
||last\_execution|TIMESTAMP|Marca de tiempo de la última inferencia.|
|**RiskAlert** (VO)|alert\_id|UUID|PK. Mensaje inmutable de advertencia.|
||severity|ENUM|LOW, MEDIUM, HIGH, CRITICAL.|
||message|TEXT|Ej: "Alerta: 10 Quinquenios por pagar en 90 días".|
||financial\_impact|DECIMAL(15,2)|Cálculo proyectado de la multa del 30% si aplica.|
||is\_dismissed|BOOLEAN|Estado de atención por parte de RRHH.|

Entidad:

|**Entidad**|**Campo**|**Tipo de Dato**|**Restricciones / Lógica**|
| :- | :- | :- | :- |
|**SelfServiceAction**|action\_id|UUID|PK. Registro de cualquier acción iniciada por el empleado.|
||person\_id|UUID|FK a Person. Quién realiza la acción.|
||action\_type|ENUM|DATA\_UPDATE, LEAVE\_REQ, CERT\_REQ, ASSET\_REPORT.|
||payload|JSONB|Datos temporales antes de impactar el Core (Draft data).|
|**ApprovalWorkflow**|workflow\_id|UUID|PK. Instancia del motor de estados de aprobación.|
||action\_id|UUID|FK a SelfServiceAction.|
||current\_step|INT|Nivel actual de la jerarquía de aprobación.|
||history|JSONB|**Invariante SoD**: Registro de quién aprobó qué y cuándo.|
|**Notification**|notif\_id|UUID|PK. Registro de alertas enviadas al usuario.|
||recipient\_id|UUID|FK a Person.|
||channel|ENUM|PUSH\_MOBILE, EMAIL, IN\_APP.|
||read\_at|TIMESTAMP|Fecha y hora de lectura para auditoría de notificación legal.|

Event Outbox: Tabla necesaria para garantizar que los eventos de este contexto (como PERSON\_CREATED o POSITION\_VACATED) lleguen correctamente a la IA y otros módulos sin pérdida de consistencia.

|**Campo**|**Tipo de Dato**|**Descripción**|
| :- | :- | :- |
|event\_id|UUID|PK del evento.|
|aggregate\_type|VARCHAR(50)|Ej: "Person", "Position".|
|aggregate\_id|UUID|ID de la entidad raíz afectada.|
|event\_type|VARCHAR(100)|Ej: PERSON\_MASTER\_CREATED, POSITION\_ASSIGNED.|
|payload|JSONB|Contenido completo del evento para el RAG/IA.|
|status|ENUM|PENDING, PROCESSED, FAILED.|
|created\_at|TIMESTAMP|Marca de tiempo de creación.|


