# Fase B --- Matriz de Variabilidad Multi-Tenant

*Core HR Bolivia 2026 · Plataforma multi-tenant: ONG/Fundaciones,
Retail, Educación y Corporativo*

Fuente: Documentacion_modulo_core_hr.docx

+----+----------------+---------------+---------------+---------------+
| *  | **ONG /        | **Retail      | **Educación** | **            |
| *P | Fundaciones**  | (Comercial)** |               | Corporativo** |
| ar |                |               |               |               |
| ám |                |               |               |               |
| et |                |               |               |               |
| ro |                |               |               |               |
| ** |                |               |               |               |
+====+================+===============+===============+===============+
| *  | -   Budget     | -   Social    | -   Talent    | -   Position  |
| *1 |     Allocation |     Security  |     Inventory |     &         |
| ·  |     & Funding  |     &         |     &         |     Headcount |
| M  |     Control    |               |               |               |
| ód |                |    Regulatory |   Performance |    Management |
| ul |    \[crítico\] |               |               |               |
| os |                |    Compliance |   Foundations |   \[crítico\] |
| A  | -   Compliance |               |               |               |
| ct |     & Policy   |   \[crítico\] |   \[crítico\] | -   Talent    |
| iv |     Engine     |               |               |     Inventory |
| os |                | -   Digital   | -             |     &         |
| ** | -   Employment |     Kardex    |    Employment |               |
|    |                |     (carnet   |               |   Performance |
|    |   Relationship |               |  Relationship |               |
|    |     &          |    sanitario) |     (Acad     |   Foundations |
|    |     Lifecycle  |               | emicProfile + |               |
|    |                | -   ESS / MSS |     W         | -   Contracts |
|    | -   Leave,     |     (volumen  | orkerProfile) |     & Legal   |
|    |     Absences & |     masivo)   |               |               |
|    |                |               | -   Digital   |    Employment |
|    |    Permissions | -   Assets &  |     Kardex    |     Terms     |
|    |     (misiones  |     Equipment |     (títulos, |               |
|    |     de campo)  |               |               | -             |
|    |                |    Assignment |    escalafón) |    Seniority, |
|    | -   Digital    |               |               |     Benefits  |
|    |     Kardex &   |   (terminales | -   Leave &   |     &         |
|    |     Document   |     móviles)  |     Absences  |     Accruals  |
|    |     Compliance |               |     (receso   |               |
|    |                | -   Leave &   |               | -   Social    |
|    | -   Workflow,  |     Absences  |    académico) |     Security  |
|    |     Audit &    |               |               |     &         |
|    |     Legal      | -             | -             |               |
|    |     Evidence   |    Compliance |    Seniority, |    Regulatory |
|    |                |     & Policy  |     Benefits  |               |
|    | -   AI         |     Engine    |     &         |    Compliance |
|    |     Insights & |               |     Accruals  |               |
|    |     Predictive |               |               | -   AI        |
|    |     Analytics  |               | -             |     Insights  |
|    |     (riesgo    |               |    Compliance |               |
|    |                |               |     & Policy  |    (sucesión, |
|    |    agotamiento |               |     Engine    |     churn)    |
|    |     donante)   |               |               |               |
+----+----------------+---------------+---------------+---------------+
| *  | -   **P1** --- | -   **P1**    | -   **P1**    | -   **P1**    |
| *2 |     Base       |     --- Base  |     --- Base  |     --- Base  |
| ·  |     antigüedad |               |               |               |
| Re |     = 1 SMN    |    antigüedad |    antigüedad |    antigüedad |
| gl |     (Bs 3.300) |     = 3 SMN   |     = 1 SMN   |     = 3 SMN   |
| as |                |     (Bs       |     (Bs       |     (Bs       |
| A  | -   **P2** --- |     9.900)    |     3.300)    |     9.900)    |
| ct |     Exención   |               |               |               |
| iv |     total de   | -   **P2**    | -   **P2**    | -   **P2**    |
| as |     Prima      |     ---       |     ---       |     ---       |
| ** |     Anual      |     Provisión |     Exención  |     Provisión |
|    |                |     mensual   |     total de  |     mensual   |
|    | -   **P8** --- |     Prima     |     Prima     |     Prima     |
|    |     Alerta     |     8,33%     |     Anual     |     8,33%     |
|    |     Quinquenio |               |               |               |
|    |     60 meses + | -   **P5**    | -   **P3**    | -   **P5**    |
|    |     multa 30%  |     ---       |     ---       |     ---       |
|    |                |     Límite    |     Multi-Rol |     Límite    |
|    | -   **P9** --- |     jornada   |               |     jornada   |
|    |     INFOCAL    |     género    |   Académico-A |     género    |
|    |                |     (48h/40h) | dministrativo |               |
|    |    desactivado |               |     (solo     | -   **P6**    |
|    |     (toggle    | -   **P6**    |               |     ---       |
|    |     off)       |     ---       |    educación) |     Trabajo   |
|    |                |     Recargo   |               |     dominical |
|    | -   **P11**    |     nocturno  | -   **P4**    |     recargo   |
|    |     ---        |               |     ---       |     100%      |
|    |     Viáticos y |  20:00--06:00 |               |               |
|    |     misiones   |     (25--50%) |  Contratación | -   **P8**    |
|    |     de campo   |               |     por carga |     ---       |
|    |                | -   **P7**    |     horaria   |     Alerta    |
|    | -   **P12**    |     ---       |     semestral |               |
|    |     ---        |     Vigencia  |               |  Quinquenio + |
|    |     Prevención |               | -   **P6**    |     multa 30% |
|    |     tácita     |   documental: |     ---       |               |
|    |                |     carnet    |     Trabajo   | -   **P9**    |
|    |   reconducción |               |     dominical |     ---       |
|    |     (90 días)  |   sanitario + |     con       |     INFOCAL   |
|    |                |               |     recargo   |     Santa     |
|    | -   **P13**    |  antecedentes |     100%      |     Cruz 1%   |
|    |     --- Escala |               |               |     activo    |
|    |     vacaciones | -   **P8**    | -   **P7**    |               |
|    |     15/20/30   |     ---       |     ---       | -   **P10**   |
|    |     días       |     Alerta    |     Vigencia  |     ---       |
|    |                |               |               |     Feriados  |
|    | -   **P14**    |  Quinquenio + |   documental: |     de        |
|    |     ---        |     multa 30% |     títulos + | partamentales |
|    |                |               |     escalafón |               |
|    |  Mantenimiento | -   **P9**    |               | -   **P13**   |
|    |     de valor   |     ---       | -   **P8**    |     ---       |
|    |     UFV        |     INFOCAL   |     ---       |     Escala    |
|    |                |     Santa     |     Alerta    |               |
|    | -   **P15**    |     Cruz 1%   |               |    vacaciones |
|    |     ---        |     activo    |    Quinquenio |               |
|    |     Promedio   |               |               | -   **P15,    |
|    |     90 días    | -   **P10**   | -   **P9**    |     P16, P17, |
|    |     (base      |     ---       |     ---       |     P18** --- |
|    |     finiquito) |     Feriados  |     INFOCAL   |               |
|    |                |     de        |               |   Universales |
|    | -   **P16**    | partamentales |   desactivado |               |
|    |     ---        |     (24 sep)  |     (toggle   |               |
|    |                |               |     off)      |               |
|    |    Aguinaldo + | -   **P13**   |               |               |
|    |     duodécimas |     ---       | -   **P11**   |               |
|    |                |     Escala    |     ---       |               |
|    | -   **P17**    |               |     Viáticos  |               |
|    |     ---        |    vacaciones |     y recesos |               |
|    |     Finiquito  |               |               |               |
|    |     /          | -   **P15**   |    académicos |               |
|    |     desahucio  |     ---       |               |               |
|    |     15 días    |     Promedio  | -   **P12**   |               |
|    |                |     incluye   |     ---       |               |
|    | -   **P18**    |               |               |               |
|    |     --- RC-IVA |  comisiones + |    Prevención |               |
|    |     13% (Ley   |     recargos  |     tácita    |               |
|    |                |     nocturnos |               |               |
|    | Transparencia) |               |  reconducción |               |
|    |                | -   **P16,    |               |               |
|    |                |     P17,      | -   **P13**   |               |
|    |                |     P18** --- |     ---       |               |
|    |                |               |     Escala    |               |
|    |                |   Universales |               |               |
|    |                |               |  vacaciones + |               |
|    |                |               |     Receso    |               |
|    |                |               |     Académico |               |
|    |                |               |     colectivo |               |
|    |                |               |               |               |
|    |                |               | -   **P15,    |               |
|    |                |               |     P16, P17, |               |
|    |                |               |     P18** --- |               |
|    |                |               |               |               |
|    |                |               |   Universales |               |
+----+----------------+---------------+---------------+---------------+
| *  | -   Bono       | -   Bono      | -   Bono      | -   Bono      |
| *3 |                |               |               |               |
| ·  |    antigüedad: |   antigüedad: |   antigüedad: |   antigüedad: |
| Fó |     % × 1 SMN  |     % × 3 SMN |     % × 1 SMN |     % × 3 SMN |
| rm |     (Bs 3.300) |     (Bs       |     (Bs       |     (Bs       |
| ul |                |     9.900)    |     3.300)    |     9.900)    |
| as | -   Pri        |               |               |               |
| ** | ma/Utilidades: | -   Provisión | -   Prim      | -   Provisión |
|    |     exención   |     Prima:    | a/Utilidades: |     Prima:    |
|    |     total ---  |     s         |     exención  |     s         |
|    |     no se      | alarioMensual |     total     | alarioMensual |
|    |     provisiona |     × 8,33%   |               |     × 8,33%   |
|    |                |               | -   Contrato  |               |
|    | -              | -   Prorrateo |     docente:  | -   Prorrateo |
|    |    Quinquenio: |     Prima: si |     vigencia  |     Prima: si |
|    |     promedi    |     25%       |     =         |     25%       |
|    | o(TotalGanado, |               |     semestre  |               |
|    |     -90d) ×    |  utilidadNeta |     académico |  utilidadNeta |
|    |     años       |     \< 1      |               |     \< 1      |
|    |                |     sueldo,   | -   Horas     |     sueldo    |
|    | -   RC-IVA:    |               |     cátedra:  |               |
|    |                |    distribuir |     suel      | -   INFOCAL:  |
|    |   (NetoMensual |     eq        | doHoraCátedra |     s         |
|    |     − 2×SMN) × | uitativamente |     ×         | alarioMensual |
|    |     13% −      |               |               |     × 1%      |
|    |     13%×SMN    | -   Recargo   |  cargaHoraria |               |
|    |                |     nocturno: |     (separado | -   Recargo   |
|    | -   Retención  |               |     del       |               |
|    |     Gestora:   |   salarioHora |     admin)    |    dominical: |
|    |                |     × (1,25 ó |               |               |
|    |    TotalGanado |     1,50)     | -   Recargo   |   salarioHora |
|    |     × 12,71%   |               |               |     × 2,00    |
|    |                | -   Recargo   |    dominical: |               |
|    | -   L          |               |               | -             |
|    | aborCostSplit: |    dominical: |   salarioHora |   Quinquenio: |
|    |     Σ          |               |     × 2,00    |     promedio  |
|    |                |   salarioHora |               | (TotalGanado, |
|    |    porcentajes |     × 2,00    | -             |     -90d) ×   |
|    |     por        |               |    Vacaciones |     años      |
|    |     donante =  | -   INFOCAL:  |     Receso:   |               |
|    |     100%       |     s         |     consumo   | -   Retención |
|    |                | alarioMensual |     colectivo |     Gestora:  |
|    | -   Viáticos:  |     × 1%      |     ---       |               |
|    |     excluidos  |               |     bloquea   |   TotalGanado |
|    |     del Total  | -   Promedio  |               |     × 12,71%  |
|    |     Ganado     |               |   solicitudes |               |
|    |     para       |    finiquito: |               | -   RC-IVA:   |
|    |     aportes    |     incluye   |  individuales |               |
|    |                |               |               | procedimiento |
|    | -   Multa      |  comisiones + | -             |               |
|    |     finiquito: |     recargos  |   Quinquenio: |    escalonado |
|    |                |     últimos   |     promedio  |     Ley       |
|    |   totalLíquido |     90 días   | (TotalGanado, |               |
|    |     × 30% si   |               |     -90d) ×   | Transparencia |
|    |     \> 15 días | -             |     años      |               |
|    |                |   Quinquenio: |               | -   Provisión |
|    | -   Multa      |     promedio  | -   Retención |     contable: |
|    |                | (TotalGanado, |     Gestora:  |     desde día |
|    |    quinquenio: |     -90d) ×   |               |     91        |
|    |     monto ×    |     años      |   TotalGanado |     relación  |
|    |     30% si \>  |               |     × 12,71%  |     laboral   |
|    |     30 días    | -   Retención |               |               |
|    |                |     Gestora:  | -   RC-IVA:   | -   Multa     |
|    |                |               |               |               |
|    |                |   TotalGanado | procedimiento |    finiquito: |
|    |                |     × 12,71%  |               |               |
|    |                |               |    escalonado |  totalLíquido |
|    |                | -   RC-IVA:   |     Ley       |     × 30% si  |
|    |                |               |               |     \> 15     |
|    |                | procedimiento | Transparencia |     días      |
|    |                |               |               |               |
|    |                |    escalonado | -   Multa     |               |
|    |                |     Ley       |               |               |
|    |                |               |    finiquito: |               |
|    |                | Transparencia |               |               |
|    |                |               |  totalLíquido |               |
|    |                | -   Multa     |     × 30% si  |               |
|    |                |               |     \> 15     |               |
|    |                |    finiquito: |     días      |               |
|    |                |               |               |               |
|    |                |  totalLíquido |               |               |
|    |                |     × 30% si  |               |               |
|    |                |     \> 15     |               |               |
|    |                |     días      |               |               |
+----+----------------+---------------+---------------+---------------+
| *  | -              | -   **Gerente | -             | -   **Jefe de |
| *4 |  **Coordinador |     de Tienda |    **Decano** |     Área /    |
| ·  |     de         |     (Store    |     ---       |               |
| R  |     Proyecto** |               |     gestiona  |    Director** |
| ol |     ---        |    Manager)** |     Ac        |     ---       |
| es |     consulta   |     ---       | ademicProfile |     revisión  |
| AB |                |     autoridad |     de su     |     Perform   |
| AC | FundingSource, |     recursiva |     facultad; | anceSnapshot; |
| ** |                |     sobre su  |     valida    |     aprueba   |
|    | LaborCostSplit |     OrgUnit;  |     títulos y |               |
|    |     de sus     |     autoriza  |     méritos;  |   vacaciones; |
|    |     proyectos; |               |     aprueba   |     valida    |
|    |     valida     | sobretiempos; |     ascensos  |     activos   |
|    |     timesheets |     monitorea |               |               |
|    |     de campo   |     carnet    |    escalafón; | tecnológicos; |
|    |                |               |     bloqueado |     sueldo de |
|    | -   **Revisor  |    sanitario; |     de datos  |               |
|    |     de Fondos  |     bloqueado |     bancarios |  subordinados |
|    |     (C         |     de        |     y         |     cifrado   |
|    | umplimiento)** |               |               |               |
|    |     --- audita |   SalaryTerms | WorkerProfile | -             |
|    |     contratos, |               |     a         |    **Analista |
|    |     ejecución  | -             | dministrativo |     de        |
|    |     p          |    **Analista |               |               |
|    | resupuestaria; |     de        | -   **Docente |   Planillas** |
|    |     puede      |               |     (ESS      |     ---       |
|    |     suspender  |   Planillas** |     esp       |     activa    |
|    |     timesheets |     ---       | ecializado)** |     cálculo   |
|    |                |     acceso a  |     --- carga |     de        |
|    | -   **HR Super |               |               |     Desahucio |
|    |     User** --- |   TaxForm110, |  certificados |     (3        |
|    |     exige      |               |     de        |     salarios) |
|    |                |   comisiones, |               |     en        |
|    |  FundingSource |     recargos  |    postgrado; |     despido   |
|    |     para crear |     nocturnos |     ve        |     sin       |
|    |     plazas     |     en        |     materias  |     causa;    |
|    |                |     promedio  |               |     valida    |
|    | -   **Analista |               |    asignadas; |     Provisión |
|    |     de         | -   **HR      |     solicita  |     INFOCAL   |
|    |                |     Super     |               |               |
|    |    Planillas** |     User**    |    vacaciones | -   **Budget  |
|    |     --- global |     ---       |     en Receso |               |
|    |                |               |     Académico |    Controller |
|    | -              |    onboarding |               |               |
|    |  **Dependiente |     masivo    | -   **HR      |  (Finanzas)** |
|    |     ESS** ---  |               |     Super     |     ---       |
|    |     puede      |    optimizado |     User**    |     aprueba   |
|    |     tener      |     para      |     ---       |     ajustes   |
|    |     anticipo   |     velocidad |     gestiona  |               |
|    |     de         |               |     Ac        |    salariales |
|    |     vacaciones | -             | ademicProfile |     \> 15%    |
|    |     si         | **Dependiente |     paralelo  |               |
|    |     política   |     ESS** --- |     al        | -   **HR      |
|    |     lo permite |     solicita  |               |     Super     |
|    |                |               | WorkerProfile |     User**    |
|    |                |  certificados |               |     ---       |
|    |                |     en        | -             |               |
|    |                |     volumen   |    **Analista | planificación |
|    |                |     (crédito  |     de        |     carrera y |
|    |                |     bancario) |               |     sucesión  |
|    |                |               |   Planillas** |               |
|    |                |               |     ---       |               |
|    |                |               |     separa    |               |
|    |                |               |     base      |               |
|    |                |               |     imponible |               |
|    |                |               |     docente   |               |
|    |                |               |     vs        |               |
|    |                |               |     a         |               |
|    |                |               | dministrativo |               |
+----+----------------+---------------+---------------+---------------+
| *  | -   Contrato:  | -   Contrato: | -   Contrato  | -   Contrato: |
| *5 |     SoD ---    |     SoD ---   |               |     SoD ---   |
| ·  |     creador ≠  |     creador ≠ |    semestral: |     creador ≠ |
| Ap |     aprobador  |     aprobador |     Decano +  |     aprobador |
| ro |                |               |     HR (SoD)  |               |
| ba | -   Onboarding | -             |               | -   Adenda    |
| ci |     bloqueado  |  Sobretiempo: | -   Ac        |     salarial: |
| on |     sin        |               | ademicProfile |     SoD; si   |
| es |     ProjectID  |  autorización |               |     \> 15% →  |
| ** |     / DonorID  |     Gerente   |   habilitado: |               |
|    |     aprobado   |     de Tienda |     títulos   |    aprobación |
|    |                |     (respeta  |     validados |     Budget    |
|    | -   Adenda:    |     48h/40h)  |     en        |               |
|    |     SoD +      |               |               |    Controller |
|    |     validación | -   Carnet    | DigitalKardex |               |
|    |                |               |     (DOCEN    | -   Cambio de |
|    |  FundingSource |    sanitario: | T_ACADEMIC_TI |     cargo:    |
|    |                |               | TLE_VERIFIED) |     valida    |
|    | -   Renovación |    validación |               |               |
|    |     tácita:    |     RRHH      | -   **Ascenso | HeadcountPlan |
|    |     alerta 90  |     antes de  |               |     destino   |
|    |     días →     |     habilitar |    escalafón: |               |
|    |     decisión   |     turno     |     Comisión  | -   Adenda    |
|    |     HR + Legal |               |     Académica |     genera    |
|    |                | -   Adenda \> |     (Decanato |     Aviso de  |
|    | -   Viáticos:  |     15%:      |     /         |     Impacto   |
|    |     descargo   |               |     Dirección |               |
|    |     validado   |    aprobación |               |    Tributario |
|    |     por        |     Budget    |  Académica)** |     RC-IVA al |
|    |     Revisor de |               |               |     empleado  |
|    |     Fondos     |    Controller | -   Adenda    |               |
|    |                |               |     salarial  | -   Desahucio |
|    | -   Timesheet  |    (Finanzas) |     por       |     (3        |
|    |     de campo:  |               |     rango:    |               |
|    |                | -             |     dispara   |    salarios): |
|    |    Coordinador | Transferencia |     Workflow  |     validado  |
|    |     de         |     de        |     2         |     por       |
|    |     Proyecto → |     tienda:   |     (Adenda)  |     Analista  |
|    |     aprobación |               |     po        |     de        |
|    |     financiera |    validación | st-aprobación |     Planillas |
|    |                |               |     comisión  |               |
|    | -              | BudgetFunding |               | -             |
|    |    Quinquenio: |     destino   | -             |   Vacaciones: |
|    |     30 días    |               |    Renovación |     Jefe de   |
|    |     calendario | -   Ausencia: |               |     Área      |
|    |     para pago; |     MSS       |    semestral: |     (escala   |
|    |     escalada a |     Gerente   |     máx 2     |     15/20/30  |
|    |     Legal si   |     de        |               |     días)     |
|    |     vence      |     Tienda;   | renovaciones; |               |
|    |                |     si \< 48h |     luego     | -             |
|    |                |     busca     |               |   Quinquenio: |
|    |                |     suplente  |    conversión |     30 días;  |
|    |                |               |     a         |     multa 30% |
|    |                | -             |               |     escalada  |
|    |                |    Finiquito: |    indefinido |     a         |
|    |                |     15 días;  |               |     Gerencia  |
|    |                |     multa 30% | -   Receso    |     General + |
|    |                |               |               |     Legal     |
|    |                |    automática |    Académico: |               |
|    |                |     en día 16 |     RRHH      |               |
|    |                |               |     configura |               |
|    |                |               |     bloqueo   |               |
|    |                |               |     colectivo |               |
|    |                |               |     de        |               |
|    |                |               |               |               |
|    |                |               |    vacaciones |               |
|    |                |               |               |               |
|    |                |               | -   Suplencia |               |
|    |                |               |     docente:  |               |
|    |                |               |     Decano o  |               |
|    |                |               |     Dirección |               |
|    |                |               |     Académica |               |
+----+----------------+---------------+---------------+---------------+
| *  | -              | -             | -   Ac        | -   Perfor    |
| *6 |  FundingSource | DigitalKardex | ademicProfile | manceSnapshot |
| ·  |     ---        |     ---       |     --- carga |     ---       |
| F  |     do         |     carnet    |     horaria,  |               |
| ue | nante/proyecto |     sanitario |     rango,    |  evaluaciones |
| nt |                |     (Sedege   |     materias  |     de        |
| es |   (obligatorio | s/Alcaldía) + |     asignadas |     desempeño |
| de |     para crear |               |               |               |
| D  |     plazas y   |  antecedentes | -   Títulos   | -             |
| at |     contratos) |               |               | HeadcountPlan |
| os |                |    policiales |    académicos |     ---       |
| ** | -              |               |     en        |     plazas    |
|    | LaborCostSplit | -             |               |     p         |
|    |     ---        |    CostCenter | DigitalKardex | resupuestadas |
|    |                |     --- por   |               |     por área  |
|    |   distribución |     sucursal  |    (Provisión |               |
|    |     porcentual |               |     Nacional  | -             |
|    |     100% por   |    (prorrateo |     Bolivia)  |    Grade/Band |
|    |     proyecto   |     días      |               |     --- banda |
|    |                |               | -             |     salarial  |
|    | -   BudgetLine |   trabajados) |  Certificados |     del cargo |
|    |     --- saldo  |               |     de        |               |
|    |     disponible | -   L         |     escalafón | -             |
|    |     del        | aborCostSplit |     docente   |    TaxForm110 |
|    |     proyecto   |     --- split |               |     ---       |
|    |     (gatilla   |               | -             |     facturas  |
|    |     FUNDI      |    automático |    Calendario |     RC-IVA +  |
|    | NG_SOURCE_PROJ |     en        |     académico |     SIAT      |
|    | ECT_EXHAUSTED) |     t         |               |               |
|    |                | ransferencias |   (semestres, | -   Ho        |
|    | -   H          |     mid-mes   |     recesos)  | lidayCalendar |
|    | olidayCalendar |               |               |               |
|    |                | -   Registros | -   SkillSet  |   (Nacional + |
|    |    (Nacional + |     de        |     /         |     SCZ)      |
|    |     SCZ 24     |     marcación |     Tr        |               |
|    |     sep)       |     horaria + | ainingHistory | -   SocialSe  |
|    |                |     ge        |     ---       | curityAccount |
|    | -   Timesheets | olocalización |     méritos   |     ---       |
|    |     de         |     (OrgUni   |               |     NUA/CUA   |
|    |     misiones   | t.geo_coords) |    acumulados |     Gestora   |
|    |     de campo   |               |               |     Pública   |
|    |                | -             | -   Ho        |               |
|    | -              |    Comisiones | lidayCalendar | -   Labo      |
|    |    Formularios |     y         |               | rCostForecast |
|    |     de         |     recargos  |   (Nacional + |     (IA) ---  |
|    |     descargo   |     nocturnos |     SCZ)      |               |
|    |     de         |               |               |   provisiones |
|    |     viáticos   |    percibidos | -             |               |
|    |                |     (para     |  AccrualVault |    quinquenio |
|    | -              |     promedio  |     ---       |               |
|    |   AccrualVault |     90 días)  |               |    vegetativo |
|    |     --- saldo  |               |    antigüedad |               |
|    |                | -   Ho        |     continua  |               |
|    |   vacaciones + | lidayCalendar |     para      |               |
|    |     quinquenio |               |     escalafón |               |
|    |                |   (Nacional + |               |               |
|    |                |     SCZ)      |               |               |
|    |                |               |               |               |
|    |                | -             |               |               |
|    |                |    TaxForm110 |               |               |
|    |                |     ---       |               |               |
|    |                |     facturas  |               |               |
|    |                |     RC-IVA +  |               |               |
|    |                |     SIAT      |               |               |
+----+----------------+---------------+---------------+---------------+
| *  | -   Donantes / | -   SEDEGES / | -             | -             |
| *7 |     organismos |     Alcaldía  |    Ministerio |    Ministerio |
| ·  |                |     SCZ ---   |     de        |     de        |
| I  |    cooperación |               |     Educación |     Trabajo   |
| nt |                |    validación |     Bolivia   |               |
| eg |  internacional |     carnet    |     --- base  |  (finiquitos, |
| ra |                |     sanitario |     de datos  |               |
| ci |    (validación |               |     títulos   |   auditorías) |
| on |     DonorID)   | -             |               |               |
| es |                |    Ministerio |    (Provisión | -   Fundación |
| /  | -   Ministerio |     de        |     Nacional) |     INFOCAL   |
| Co |     de Trabajo |     Trabajo   |               |     Santa     |
| nt |                |               | -             |     Cruz ---  |
| ac |   (auditorías, | (inspecciones |    Ministerio |     aporte 1% |
| to |     AuditLog   |     ---       |     de        |               |
| ** |     WORM)      |     AuditLog  |     Trabajo   | -   Gestora   |
|    |                |     WORM)     |               |     Pública   |
|    | -   Gestora    |               | -   Gestora   |     (12,71%)  |
|    |     Pública    | -   Fundación |     Pública   |               |
|    |     (12,71%    |     INFOCAL   |     (12,71%)  | -   SIAT /    |
|    |     --- baja   |     Santa     |               |     SIN ---   |
|    |     en         |     Cruz ---  | -   SIAT /    |     RC-IVA    |
|    |     d          |     aporte 1% |     SIN ---   |               |
|    | esvinculación) |               |     RC-IVA    | -   Entidades |
|    |                | -   Gestora   |               |     bancarias |
|    | -   SIAT / SIN |     Pública   | -   Entidades |               |
|    |     --- RC-IVA |     (12,71%)  |     bancarias | -   Módulo IA |
|    |                |               |               |     / RAG --- |
|    |   (TaxForm110) | -   SIAT /    | -             |               |
|    |                |     SIN ---   |    Calendario |    predicción |
|    | -   Entidades  |     RC-IVA    |     académico |     churn,    |
|    |     bancarias  |               |               |               |
|    |                | -   Entidades | institucional |   provisiones |
|    |    (dispersión |     bancarias |               |               |
|    |     haberes    |     (BISA,    |    (semestres |    quinquenio |
|    |     ---        |     BNB,      |     →         |               |
|    |     BANK_A     |     Mercantil |     Con       | -   ATS       |
|    | CCOUNT_SYNCED) |     Santa     | tractAddendum |     externo + |
|    |                |     Cruz)     |               |     Talent    |
|    | -   ATS        |               |   automático) |     Inventory |
|    |     externo    | -   Módulo    |               |     (sucesión |
|    |     (API       |               | -   Módulo    |     interna)  |
|    |     onboarding |    Scheduling |               |               |
|    |     vía        |     / Turnos  |    Scheduling | -   Portal    |
|    |     P          |               |     académico |               |
|    | ERSON_CREATED) |    (bloqueado |     (ELIGIBIL |    validación |
|    |                |     por       | ITY_SUSPENDED |     QR        |
|    | -   Portal     |     ELIGIBILI |     por       |               |
|    |     validación | TY_SUSPENDED) |     título    |               |
|    |     QR         |               |     vencido)  |               |
|    |     (CERTIFIC  | -   ATS       |               |               |
|    | ATE_GENERATED) |     externo   | -   Portal    |               |
|    |                |     (flujo    |               |               |
|    |                |     masivo    |    validación |               |
|    |                |               |     QR        |               |
|    |                | contratación) |               |               |
|    |                |               |               |               |
|    |                | -   Portal    |               |               |
|    |                |               |               |               |
|    |                |    validación |               |               |
|    |                |     QR        |               |               |
|    |                |               |               |               |
|    |                | (certificados |               |               |
|    |                |     micr      |               |               |
|    |                | ofinancieros) |               |               |
+----+----------------+---------------+---------------+---------------+
| *  | -   **Bloqueo  | -   **Bloqueo | -   **Bloqueo | -   **Control |
| *8 |                |               |               |     de        |
| ·  |  onboarding:** |  sanitario:** |  académico:** |     plazas:** |
| R  |     sin        |     Cajero →  |     Ac        |               |
| es |                |     Activo    | ademicProfile |    asignación |
| tr |  FundingSource |     requiere  |     requiere  |     bloqueada |
| ic |     válida y   |     carnet    |     título    |     si        |
| ci |     con saldo  |     sanitario |     validado  |     H         |
| on |     → detenido |     vigente;  |     antes de  | eadcountPlan. |
| es |                |               |     firmar    | current_slots |
| (  | -              |   vencimiento |     contrato  |     =         |
| In | **Consistencia |     →         |     docente   |     max_slots |
| va |     100%:** Σ  |     ELIGIBIL  |               |               |
| ri |                | ITY_SUSPENDED | -   **No      | -   **Base    |
| an | LaborCostSplit |               |     traslape  |               |
| te |     = 100%;    | -   **Base    |               |    antigüedad |
| s) |     cualquier  |               |   primario:** |     3 SMN:**  |
| ** |     desviación |    antigüedad |     dos       |               |
|    |     bloquea    |     3 SMN:**  |     vínculos  |   inalterable |
|    |                |               |     primarios |               |
|    | -   **Base     |   inalterable |     full-time | -   **INFOCAL |
|    |     antigüedad |               |               |     activo:** |
|    |     1 SMN:**   | -   **Límite  |   simultáneos |     1% no     |
|    |                |               |     →         |               |
|    |    inalterable |    jornada:** |     bloqueado |  desactivable |
|    |     por        |     \> 48h    |               |     para Corp |
|    |                |     (varón) o | -   **        |     en SCZ    |
|    |  configuración |     \> 40h    | Multi-vínculo |               |
|    |                |     (mujer) → |               | -   **Piso    |
|    | -   **Exención |     re        |   validado:** |               |
|    |     Prima:**   | clasificación |     suma      |   salarial:** |
|    |     sistema    |     como      |     horas     |     básico ≥  |
|    |     anula      |     horas     |     Wo        |     Bs 3.300  |
|    |     provisión  |     extra     | rkerProfile + |               |
|    |     a          |               |     Ac        | -   **SoD     |
|    | utomáticamente | -   **Recargo | ademicProfile |     adenda:** |
|    |                |               |     no puede  |     creador ≠ |
|    | -   **Viáticos |   nocturno:** |               |               |
|    |                |     i         |    colisionar |    aprobador; |
|    |   excluidos:** | dentificación |               |     \> 15% →  |
|    |     no forman  |               | -   **Receso  |     Budget    |
|    |     parte del  |    automática |               |               |
|    |     Total      |               |  Académico:** |    Controller |
|    |     Ganado     |  20:00--06:00 |     bloqueo   |               |
|    |     para       |     sin       |               |   obligatorio |
|    |     aportes    |               |   solicitudes |               |
|    |                |   posibilidad |               | -             |
|    | -   **Piso     |     de omitir |  individuales |   **Effective |
|    |                |               |     de        |     Dating:** |
|    |    salarial:** | -   **INFOCAL |               |     adendas   |
|    |     contrato   |     activo:** |    vacaciones |     con       |
|    |     bloqueado  |     1% no     |     durante   |     vigencias |
|    |     si básico  |               |     semestre  |               |
|    |     \< Bs      |  desactivable |               |  superpuestas |
|    |     3.300      |     para      | -   **Límite  |     →         |
|    |                |     Retail en |     re        |               |
|    | -   **SoD:**   |     SCZ       | novaciones:** |   prohibidas; |
|    |     creador ≠  |               |     máx 2     |     cambios   |
|    |     aprobador  | -   **Split   |     contratos |     lineales  |
|    |     en todo    |               |     plazo     |               |
|    |     flujo      |  prorrateo:** |     fijo; al  |  obligatorios |
|    |     financiero |               |     3° →      |               |
|    |                | transferencia |               | -             |
|    | -   **I        |     mid-mes → |    conversión |   **Deducción |
|    | nalterabilidad |     L         |     forzada a |     Gestora   |
|    |                | aborCostSplit |               |     exacta:** |
|    |    AuditLog:** |     debe      |    indefinido |     12,71%    |
|    |     registros  |     sumar     |               |     fijo      |
|    |     cerrados   |     100%      | -             |     sobre     |
|    |     no         |               |    **Vigencia |     Total     |
|    |                | -             |     cer       |     Ganado;   |
|    |   modificables |    **Promedio | tificación:** |     no        |
|    |     (WORM)     |               |               |               |
|    |                | comisiones:** | certificación |  configurable |
|    | -   **Multa    |     P15       |     expirada  |               |
|    |     30%:**     |     obliga    |     →         | -             |
|    |                |     incluir   |               |   **Provisión |
|    |   autoactivada |     variables |    asignación |     desde día |
|    |     al día 16  |     del       |     a puestos |     91:**     |
|    |     finiquito  |     trimestre |     críticos  |     provisión |
|    |     / día 31   |               |               |     contable  |
|    |     quinquenio | -   **Piso    |    invalidada |     de        |
|    |                |               |               |               |
|    | -              |   salarial:** | -   **Base    | indemnización |
|    |    **Identidad |     básico ≥  |               |     inicia    |
|    |     única:**   |     Bs 3.300  |    antigüedad |     obl       |
|    |     una Person |               |     1 SMN:**  | igatoriamente |
|    |     ID por CI; | -   **Activos |               |               |
|    |                |               |   inalterable | -   **Activos |
|    |  deduplicación | pendientes:** |               |               |
|    |     bloqueante |     OFF       | -             | pendientes:** |
|    |                | BOARDING_BLOC |    **Exención |     OFF       |
|    |                | KED_BY_ASSETS |     Prima:**  | BOARDING_BLOC |
|    |                |     hasta     |     sistema   | KED_BY_ASSETS |
|    |                |               |     anula     |               |
|    |                |    devolución |     provisión | -             |
|    |                |     total     |               | **Neutralidad |
|    |                |               | -   **Piso    |     IA:**     |
|    |                | -   **Multa   |               |     motor     |
|    |                |     30%:**    |   salarial:** |               |
|    |                |               |     básico ≥  |    predictivo |
|    |                |  autoactivada |     Bs 3.300  |     no puede  |
|    |                |     al día 16 |               |     sugerir   |
|    |                |     finiquito | -   **SoD     |     acciones  |
|    |                |               |     Comisión  |     que       |
|    |                |               |               |     violen    |
|    |                |               |  Académica:** |               |
|    |                |               |     ascenso   |   invariantes |
|    |                |               |     de rango  |     legales   |
|    |                |               |     requiere  |               |
|    |                |               |               |               |
|    |                |               |    validación |               |
|    |                |               |     colegiada |               |
+----+----------------+---------------+---------------+---------------+

**Nota:** Los invariantes marcados como \[inalterable\] no pueden
modificarse por configuración de tenant. El Strategy Pattern inyecta
reglas por perfil en el motor de validación (Compliance & Policy
Engine). SMN 2026 = Bs 3.300. DXA = unidades internas DOCX.
