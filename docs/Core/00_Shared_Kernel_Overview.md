# 00_Shared_Kernel_Overview.md

## Integrantes
* Edgar Chavez Rojas
* Adrián Flores Arteaga
* Jose María Méndez Ramos
* Rafaela Ribera Velasquez
* Katherine Vargas Eguez

## Propósito General
Este documento resume la estructura lógica de la plataforma **PeopleCoreIA**, diseñada para soportar los perfiles de ONG, Retail, Corporativo y Educación en Bolivia.

---

## I. Mapa de Dominios (Los 16 Pilares con Invariantes)

1. **Domain: Organization & Workforce Structure**
   * **Propósito:** Definir la infraestructura lógica, territorial y de centros de costo.
   * **Entidades Clave:** `OrgUnit`, `OrgHierarchy`, `CostCenter`, `Campus/Sede`.
   * **Lógica Enterprise:** Soporta jerarquías múltiples (administrativa, funcional, académica).
   * **Invariantes:** * **No Unidades Huérfanas:** Toda unidad (excepto la raíz) debe pertenecer a una jerarquía; evita empleados sin dependencia clara.
     * **Unicidad de Jefe Administrativo:** Un colaborador no puede tener dos jefes administrativos simultáneos para evitar conflictos de aprobación.

2. **Domain: Person & Identity**
   * **Propósito:** Gestionar la identidad civil única del ser humano.
   * **Entidades Clave:** `Person`, `PartyIdentifier` (CI, Pasaporte), `ContactPoint`.
   * **Lógica Enterprise:** Reglas de deduplicación para evitar registros duplicados de una misma identidad civil.
   * **Invariantes:**
     * **Identidad Única:** Una persona física solo posee un registro maestro (`Person ID`), vital para el cálculo de antigüedad en reingresos.

3. **Domain: Employment Relationship & Lifecycle**
   * **Propósito:** Administrar el vínculo legal y operativo (onboarding, movimientos, offboarding).
   * **Entidades Clave:** `Relationship`, `WorkerProfile`, `AcademicProfile`.
   * **Lógica Enterprise:** Soporta el "Multi-vínculo" (ej. administrativo y docente simultáneo).
   * **Invariantes:**
     * **No Traslape de Vínculos Primarios:** Impide dos vínculos laborales primarios de tiempo completo activos para evitar contingencias legales por doble percepción.

4. **Domain: Position & Headcount Management**
   * **Propósito:** Controlar las plazas presupuestadas y descriptores de cargo.
   * **Entidades Clave:** `Job`, `Position`, `Grade/Band`.
   * **Lógica Enterprise:** Vincula cada posición a una fuente de financiamiento, esencial para ONGs.
   * **Invariantes:**
     * **Control de Plazas (Headcount):** Impide asignar personal a una posición sin plaza vacante autorizada presupuestariamente.

5. **Domain: Contracts & Legal Employment Terms**
   * **Propósito:** Traducir la legislación laboral en reglas digitales y vigencias.
   * **Entidades Clave:** `Contract`, `ContractAddendum`, `EligibilityMatrix`.
   * **Lógica Enterprise:** Objeto vivo que dispara reglas de jornada y beneficios.
   * **Invariantes:**
     * **Integridad Temporal (As-Of):** Prohíbe adendas con vigencias superpuestas; los cambios deben ser lineales para cálculos retroactivos.
     * **Estado Aprobado:** Un contrato solo se activa si posee evidencia firmada y aprobación mediante flujo de Segregación de Funciones (SoD).

6. **Domain: Digital Kardex & Document Compliance**
   * **Propósito:** Asegurar validez documental y cumplimiento de requisitos industriales.
   * **Entidades Clave:** `DocumentRecord`, `ComplianceRequirement`, `ValidationStatus`.
   * **Lógica Enterprise:** Motor de estados de cumplimiento (Pendiente, Validado, Expirado).
   * **Invariantes:**
     * **Bloqueo por Ilicitud:** Cargos críticos (ej. Cajero) no pasan a "Activo" sin validación de documentos obligatorios (antecedentes/garantías).

7. **Domain: Assets & Equipment Assignment**
   * **Propósito:** Rastrear responsabilidad del trabajador sobre bienes de la empresa.
   * **Entidades Clave:** `AssignedAsset`, `AssetLog`.
   * **Lógica Enterprise:** Automatiza el control de activos en procesos de transferencia o baja.
   * **Invariantes:**
     * **Certificación de Devolución:** El proceso de liquidación no cierra financieramente si existen activos de alto valor marcados como "No Devueltos" sin justificación.

8. **Domain: Employee & Manager Self-Service (ESS/MSS)**
   * **Propósito:** Interfaz descentralizada para autogestión y aprobaciones.
   * **Entidades Clave:** `SelfServiceAction`, `ApprovalWorkflow`, `Notification`.
   * **Lógica Enterprise:** Acceso móvil nativo con seguridad y cifrado punto a punto.
   * **Invariantes:**
     * **Jerarquía de Aprobación:** Ninguna solicitud con impacto financiero se auto-aprueba; requiere validación de un nivel superior.

9. **Domain: Compliance & Policy Engine**
   * **Propósito:** Centralizar leyes bolivianas actualizadas (2026).
   * **Entidades Clave:** `PolicyRule`, `LegalThreshold`, `RuleOverride`.
   * **Lógica Enterprise:** Permite actualizar parámetros legales (ej. SMN Bs 3.300) sin alterar código.
   * **Invariantes:**
     * **Piso Salarial Legal:** Bloquea contratos con haber básico inferior al SMN vigente (Bs 3.300).
     * **Tope de Jornada por Género:** Impide programar más de 40h semanales para mujeres sin clasificarlas como horas extra.

10. **Domain: Budget Allocation & Funding Control**
    * **Propósito:** Imputar costos laborales a proyectos o sucursales.
    * **Entidades Clave:** `FundingSource`, `BudgetLine`, `LaborCostSplit`.
    * **Lógica Enterprise:** Garantiza que no existan puestos sin respaldo presupuestario.
    * **Invariantes:**
      * **Consistencia del 100%:** La suma de la distribución porcentual de un sueldo entre proyectos debe ser exactamente el 100%.

11. **Domain: Leave, Absences & Permissions (Base)**
    * **Propósito:** Administrar descansos, vacaciones y días compensatorios.
    * **Entidades Clave:** `LeaveRequest`, `AccrualBalance`, `HolidayCalendar`.
    * **Lógica Enterprise:** Automatiza feriados nacionales y departamentales (ej. 24 de septiembre en SCZ).
    * **Invariantes:**
      * **No Saldo Negativo Forzado:** Impide solicitar vacaciones que excedan el saldo acumulado salvo política explícita de anticipo.

12. **Domain: Workflow, Audit & Legal Evidence**
    * **Propósito:** Trazabilidad inalterable y segregación de funciones (SoD).
    * **Entidades Clave:** `AuditLog`, `WorkflowInstance`, `ElectronicEvidence`.
    * **Lógica Enterprise:** Almacena el "diff" histórico de cambios salariales o contractuales.
    * **Invariantes:**
      * **Inalterabilidad de Auditoría:** Los registros de auditoría cerrados no pueden ser modificados, garantizando defensa ante juicios laborales.

13. **Domain: Seniority, Benefits & Accruals**
    * **Propósito:** Monitorear beneficios de largo plazo e indexación legal.
    * **Entidades Clave:** `SeniorityRecord`, `QuinquenioProvision`, `BenefitAccrual`.
    * **Lógica Enterprise:** Detección automática de consolidación del Quinquenio (60 meses).
    * **Invariantes:**
      * **Base de Cálculo Inviolable:** Bono de antigüedad en ONGs sobre 1 SMN y en Privadas sobre 3 SMN; inalterable por configuración.

14. **Domain: Social Security & Regulatory Compliance**
    * **Propósito:** Administrar afiliaciones y aportes a entes gestores.
    * **Entidades Clave:** `SocialSecurityAccount`, `HealthProvider`, `TaxForm110`.
    * **Lógica Enterprise:** Implementa la tasa del 13% del "IVA Transparente" para RC-IVA.
    * **Invariantes:**
      * **Deducción Laboral Exacta:** Retención para la Gestora Pública fijada estrictamente en 12,71% sobre el Total Ganado.

15. **Domain: Talent Inventory & Performance Foundations**
    * **Propósito:** Expediente cualitativo del crecimiento y competencias.
    * **Entidades Clave:** `PerformanceSnapshot`, `SkillSet`, `TrainingHistory`.
    * **Lógica Enterprise:** Escalafón docente y acreditaciones académicas para el sector educación.
    * **Invariantes:**
      * **Vigencia de Certificación:** Invalida asignación a puestos críticos si la certificación profesional ha expirado.

16. **Domain: AI Insights & Predictive Analytics**
    * **Propósito:** Predecir riesgos legales y financieros mediante patrones de datos.
    * **Entidades Clave:** `TrendPrediction`, `LaborCostForecast`, `ComplianceAlert`.
    * **Lógica Enterprise:** Alerta sobre incrementos vegetativos de antigüedad y provisiones de caja.
    * **Invariantes:**
      * **Neutralidad Algorítmica:** La IA no puede sugerir acciones que violen invariantes legales de otros dominios (ej. sueldos bajo el SMN).

---

## II. Roles de Seguridad y Accesos (RRHH)

### Roles Globales (Presentes en todos los Tenants)
1. **Super Admin (Soporte Técnico)**
   * **Módulos con Acceso:** Lectura total en los 6 Bounded Contexts.
   * **Partes Específicas:** Visualización de registros maestros, configuración de sistema y colas de eventos.
   * **Restricciones:** Datos Sensibles enmascarados/cifrados. No tiene permisos de escritura ni eliminación de logs.
   * **Lógica ABAC:** Acceso global bloqueado por invariante de inmutabilidad de logs.
2. **Administrador General / HR Super User**
   * **Módulos con Acceso:** Escritura/configuración total en Workforce, Legal, Dossier y Accruals.
   * **Partes Específicas:** Onboarding masivo, parametrización de políticas, calendarios, OrgUnit.
   * **Restricciones:** Sin acceso a APIs de banca. No puede modificar parámetros legales globales (SMN/UFV).
   * **Diferencias:** En ONG asocia `FundingSource`. En Educación gestiona `AcademicProfile`.
3. **Analista de Planillas (Payroll Specialist)**
   * **Módulos con Acceso:** Acceso total a Financial & Social y Accruals & Time-Off.
   * **Partes Específicas:** SalaryTerms, TaxForm110, promedios de finiquitos, aportes Gestora, RC-IVA.
   * **Permiso Especial:** Ve caducidad de documentos críticos (DigitalKardex). Único que valida "Despido sin Causa".
   * **Restricciones:** Sin acceso a expedientes clínicos o de desempeño no salarial.
4. **Dependiente Final (Autoservicio - ESS)**
   * **Módulos con Acceso:** Capa de Experience & IA (limitada a su perfil).
   * **Partes Específicas:** Boletas PDF, carga Form. 110, solicitud vacaciones.
   * **Restricciones:** Cifrado punto a punto. No ve datos de terceros ni estructuras superiores.

### Roles por Tenant
1. **Tenant: Retail (Comercial)**
   * **Gerente de Tienda (Store Manager):** Acceso a Workforce, Accruals, Dossier, Experience. Autoriza sobretiempos y monitorea carnet sanitario. Sin acceso a salarios o finiquitos.
2. **Tenant: ONG (Fundaciones)**
   * **Coordinador de Proyecto:** Acceso a Financial, Workforce, Accruals. Verifica viáticos y timesheets de campo. No ve planillas globales.
   * **Revisor de Fondos (Cumplimiento):** Acceso a Legal, Financial. Audita contratos y presupuestos. Suspende procesos por discrepancias.
3. **Tenant: Educación (Universidades)**
   * **Decano:** Acceso a Workforce, Dossier, Experience. Gestiona AcademicProfile y méritos académicos. Bloqueo a datos bancarios o administrativos.
   * **Docente (Perfil ESS):** Acceso a Experience, Workforce. Carga certificados, solicita vacaciones académicas. No ve expedientes de terceros.
4. **Tenant: Corporativo (Privada)**
   * **Jefe de Área / Director:** Acceso a Dossier, Accruals, Experience. Revisa desempeño, aprueba vacaciones. Encriptación total de salarios subordinados.

### Atributos para ABAC

| Atributo | Fuente | Propósito |
| :--- | :--- | :--- |
| `User.TenantID` | Sesión | Garantiza el aislamiento absoluto entre empresas. |
| `User.OrgUnitPath` | Estructura | Permite la **Autoridad Recursiva** (ver hacia abajo en el árbol). |
| `User.Location` | Perfil | Determina si el usuario puede gestionar políticas de Santa Cruz. |
| `User.Context` | Sesión | Separa el rol de "Docente" del de "Administrativo". |
| `Resource.CreatorID` | Recurso | Implementa la **Segregación de Funciones (SoD)**. |
| `Resource.IsSensitive`| Dominio | Protege datos de sueldos y memorandos. |

---

## III. Diccionario de Datos Transversal (Infraestructura)

### Event Outbox
Tabla necesaria para garantizar que los eventos lleguen correctamente a la IA y otros módulos sin pérdida de consistencia.

| Campo | Tipo de Dato | Descripción |
| :--- | :--- | :--- |
| `event_id` | UUID | PK del evento. |
| `aggregate_type` | VARCHAR(50) | Ej: "Person", "Position". |
| `aggregate_id` | UUID | ID de la entidad raíz afectada. |
| `event_type` | VARCHAR(100) | Ej: PERSON_MASTER_CREATED, POSITION_ASSIGNED. |
| `payload` | JSONB | Contenido completo del evento para el RAG/IA. |
| `status` | ENUM | PENDING, PROCESSED, FAILED. |
| `created_at` | TIMESTAMP | Marca de tiempo de creación. |