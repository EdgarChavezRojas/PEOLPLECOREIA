# Resumen de Avance: Patrón Event Outbox - Fase de Consumo (El Cartero)
**Versión:** 1.6
**Estado General:** Infraestructura Core estabilizada y Módulo Piloto conectado.

## 🏗️ Estado Actual de la Arquitectura
Se ha migrado de un enfoque de Outbox distribuido (una tabla por cada Bounded Context) a un **Outbox Global Centralizado**. La plomería principal ya está construida y probada con un módulo piloto, estableciendo las reglas de asincronismo para la integración cruzada.

## ✅ Hitos Alcanzados (Consolidados y Funcionales)

### 1. Infraestructura Compartida (`core/shared/outbox`)
Se creó el núcleo del patrón Outbox, garantizando que todo el sistema hable el mismo idioma de eventos:
* **Entidad Centralizada:** `SharedOutboxMessageJpaEntity` mapeada a la tabla `shared_event_outbox`. Maneja estado (`PENDING`, `PROCESSED`, `FAILED`) y log de errores sin setters anémicos.
* **Manejo de Lotes:** `SharedOutboxRepository` configurado para procesar lotes pequeños (`LIMIT 50`) evitando saturación de memoria.
* **El Cartero (Outbox Relay):** Un `@Scheduled` no bloqueante. Posee un bloque `try-catch` interno que asegura que si un evento falla por deserialización, se captura el error (`errorLog`) y el Cartero continúa inmediatamente con el siguiente evento del lote.

### 2. Módulo Piloto Conectado (`TimeAndBearings` / BC-TM)
Se limpió toda la implementación basura anterior de este módulo y se conectó exitosamente a la nueva infraestructura:
* **Emisión (Adapter):** Se implementó `TimeAndBearingsEventOutboxAdapter` que captura los `DomainEvent`, los convierte a JSON y los inserta en la tabla compartida de manera limpia.
* **Recepción Asíncrona:** Se configuró Spring con `@EnableAsync` y se creó un listener de prueba (`TimeAndBearingsIntegrationConsumer`).
* **Regla de Oro de Asincronismo Validada:** Los eventos dentro de un mismo BC se manejan síncronamente (`@EventListener`), mientras que los eventos de integración entre distintos BCs *deben* usar `@Async` para no bloquear el hilo del Cartero.

---

## 🛑 Problemas Detectados y Lecciones Aprendidas
* **Intento de Abstracción en Adaptadores (Fase A - Core BCs):** Se intentó crear una clase `BaseEventOutboxAdapter` para aplicar el principio DRY entre Workforce, Legal y Dossier.
* **Resultado (Revertido):** El agente de IA generó un código de reflexión (Reflection API) excesivamente complejo, frágil e inseguro para extraer los IDs de los eventos. 
* **Acción:** Se eliminó el código generado. Es preferible mantener un grado de repetición controlada (adapters explícitos) antes que introducir código "mágico" y difícil de debuggear en producción.

---

## ⏳ Backlog (Próxima Iteración)
1. **Conectar Módulo Workforce (BC-01):** Crear su Adapter y su Consumer asíncrono apuntando al `SharedOutboxRepository`, manteniendo el código simple (similar a `TimeAndBearings`).
2. **Conectar Módulo Legal (BC-02):** Implementar su plomería hacia la tabla global.
3. **Conectar Módulo Dossier (BC-03):** Implementar su plomería hacia la tabla global.
4. **Prueba Extremo a Extremo (E2E):** Ejecutar un caso de uso real donde Workforce emita un evento (ej. `RelationshipEndedEvent`), el Cartero lo despache, y TimeAndBearings reaccione revocando biometría.