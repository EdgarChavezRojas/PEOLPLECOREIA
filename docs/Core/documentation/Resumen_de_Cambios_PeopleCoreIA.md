# Resumen de Cambios en la Arquitectura PeopleCoreIA

Este documento consolida las adiciones y modificaciones realizadas a la documentación técnica para asegurar el cumplimiento del 100% con los requerimientos legales de Bolivia y los reportes institucionales analizados.

---

## 1. Cambios en: `01_Workforce_Org_Master.md`

### **Actualización en Agregado 1: PersonIdentity**
* **Campo añadido:** `marital_status` (ENUM: SOLTERO, CASADO, VIUDO, DIVORCIADO).
    * **Por qué:** Es un requisito legal obligatorio exigido en la cabecera del formulario oficial de Finiquito del Ministerio de Trabajo.
* **Campo añadido:** `profession_title` (VARCHAR).
    * **Por qué:** El formulario de Finiquito exige distinguir la "Profesión u Ocupación" civil del trabajador, independientemente de su cargo dentro de la empresa.

---

## 2. Cambios en: `02_Employment_Terms_Regulatory_Compliance.md`

### **Actualización en Agregado 5: EmploymentAgreement**
* **Campo añadido:** `employment_cond` (ENUM: PE - Permanente, PF - Plazo Fijo, JU - Jubilado).
    * **Por qué:** Estandariza la nomenclatura utilizada en los reportes tabulares de vacaciones y antigüedad, permitiendo la clasificación exacta exigida por el sistema de reportes.

---

## 3. Cambios en: `04_Accruals_Seniority_Time_Off.md`

### **Actualización en Agregado 9: AccrualVault**
* **Value Object añadido:** `SenioritySpan`.
    * **Atributos:** `years`, `months`, `days`.
    * **Por qué:** Los reportes de RRHH exigen visualizar la antigüedad exacta desglosada en años, meses y días. Este VO permite exponer el dato sin realizar cálculos pesados en la interfaz.
* **Atributos añadidos a `AccrualBalance` (Root):**
    * `initial_balance`: Registro del arrastre de gestiones pasadas.
    * `days_accrued_ytd`: Días ganados en el año actual.
    * `days_taken_ytd`: Días tomados en el año actual.
    * **Por qué:** Los reportes de saldo de vacaciones requieren ver el desglose de cómo se llegó al saldo actual (Saldo Inicial + Ganados - Tomados).

---

## 4. Cambios en: `05_Financial_Social_Compliance.md`

### **Actualización de Políticas (Clúster Fiscal y de Liquidación)**
* **Nueva Política:** `P19: Política de Segundo Aguinaldo (Esfuerzo por Bolivia)`.
    * **Por qué:** Es un concepto legal que aparece explícitamente en el formulario oficial de Finiquito. Debe estar modelado como un "Feature Toggle" condicional al crecimiento del PIB anual.

### **Actualización de Workflows (Workflow 9: Finiquito)**
* **Modificación del Flujo:** Inclusión del cálculo y generación del `IndemnizableTrimSnapshot`.
    * **Por qué:** Permite capturar de forma inmutable el detalle mes a mes del último trimestre para poblar la Tabla II del Ministerio de Trabajo.

### **Actualización del Diccionario de Datos**
* **Campo añadido en `BenefitAccrual`:** `SEGUNDO_AGUINALDO` dentro del ENUM `benefit_type`.
    * **Por qué:** Soporte para la acumulación y pago de este beneficio específico.
* **Nuevo Value Object:** `IndemnizableTrimSnapshot`.
    * **Atributos:** Desglose de `month_base` y `month_others` para cada uno de los 3 meses.
    * **Por qué:** Esencial para imprimir la sección de "Liquidación de la Remuneración Promedio Indemnizable" con los montos exactos de los últimos 90 días.
