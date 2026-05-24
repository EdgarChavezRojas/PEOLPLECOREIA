# Resumen de Correcciones - Módulo de Planillas (Payroll)

Este documento detalla las correcciones y mejoras aplicadas al módulo de Payroll para asegurar el cumplimiento con la arquitectura PeopleCoreIA y la normativa laboral boliviana 2026.

## 1. Corrección del Invariante Legal (SMN)
Se actualizó el Salario Mínimo Nacional (SMN) base para los cálculos de beneficios y bonos.
* **Valor anterior:** Bs 2.500,00
* **Valor actualizado:** **Bs 3.300,00** (Gestión 2026).
* **Archivos afectados:** `CorpPayrollStrategy.java`, `OngPayrollStrategy.java`, `RetailPayrollStrategy.java`.

## 2. Implementación de Casos de Uso Huérfanos
Se completó la lógica de orquestación para los procesos principales de la planilla que carecían de implementación.
* **Generación de Planilla (`ProcessPayrollGenerationUseCase`):** Implementado con el estado inicial correcto `BORRADOR`.
* **Aprobación de Planilla (`ProcessApprovePayrollUseCase`):** Implementado integrando la validación de Segregación de Funciones (SoD).

## 3. Integración con el Contexto de Seguridad
Se eliminó el uso de IDs aleatorios para las aprobaciones, vinculando el proceso al usuario real autenticado.
* **Mecanismo:** Extracción del ID del usuario desde `SecurityUserContext`.
* **Conversión:** Generación de un `UUID` determinístico a partir del identificador del usuario para mantener la compatibilidad con el modelo de dominio.
* **Archivo afectado:** `ProcessApprovePayrollUseCase.java`.

## 4. Alineación con Estándares AGENTS.md
Se eliminó la deuda técnica relacionada con el manejo de errores y cadenas de texto fijas.
* **Excepciones:** Se reemplazaron los `RuntimeException` genéricos por `SolverException` con códigos de error transaccionales (ej. `PAYROLL_CLOSURE_FAILED`).
* **Internacionalización (i18n):** Se eliminaron los mensajes de error hardcodeados en inglés, sustituyéndolos por llaves de i18n (ej. `PAYROLL_SOD_VIOLATION_REVIEWER`).
* **Archivos afectados:** `ProcessPayrollClosureUseCase.java`, `PayrollApproval.java`, `PayrollClosure.java`.

## 5. Refactorización de Infraestructura (MapStruct)
Se migró la capa de mapeo manual a una solución automatizada y estandarizada.
* **Cambio:** Transformación de clases de mapeo manual a interfaces `@Mapper` de MapStruct.
* **Beneficio:** Reducción de código redundante y mayor mantenibilidad.
* **Mappers actualizados:** `PayrollPeriodMapper`, `IncomeRecordMapper`, `DeductionRecordMapper`, entre otros.

## 6. Validación de Cumplimiento Bancario (Core BC-1)
Se implementó el bloqueo de seguridad para la dispersión de fondos.
* **Componente:** `EmployeeBankValidationAdapter`.
* **Lógica:** Verifica con el Módulo Core si todos los empleados tienen el evento `BANK_ACCOUNT_SYNCED` antes de permitir la generación del archivo bancario.
