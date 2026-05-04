================================================================================
BC1: WORKFORCE & ORG MASTER - README IMPLEMENTACIÓN
Versión: v1.0
Fecha: 2026-04-24
Autor: GitHub Copilot
================================================================================

## ÍNDICE GENERAL

1. Visión General
2. Archivos Generados
3. Estructura de Paquetes
4. Guía de Implementación por Fases
5. Testing & Validación
6. Notas Críticas sobre Invariantes
7. Próximos Pasos (BC2 y Superiores)

================================================================================
1. VISIÓN GENERAL
================================================================================

### Propósito
Implementar el backend del Contexto 1: "Workforce & Org Master" del módulo 
core-platform siguiendo arquitectura Hexagonal (puertos-adaptadores) y DDD.

### Scope
- **Únicamente BC1**: No incluye contratos, accruals, ni finanzas
- **Multi-Tenant**: Aislamiento lógico por tenant_id
- **PostgreSQL**: Persistencia relacional
- **Event Outbox**: Garantiza consistencia eventual

### Principios Arquitectónicos
1. **Hexagonal Architecture**: Domain → Application → Infrastructure
2. **DDD Aggregates**: Person, OrgUnit, Position, Relationship
3. **Value Objects**: ContactPoint, CostCenter, HeadcountPlan, Gender, Extension
4. **Anti-Corruption Layers**: Mappers entre capas
5. **Patrón Outbox**: Eventos inmutables para consistencia

================================================================================
2. ARCHIVOS GENERADOS (3 Documentos Versionados)
================================================================================

### Archivo 1: bc1_workforce_org_master_v1.txt (Principal)
**Contiene:**
- Domain Model completo (7 Value Objects, 9 Entities, 4 Aggregates)
- Application Layer (4 Use Cases, 8 DTOs)
- Infrastructure (Entidades JPA, Repositorios)
- REST Controllers (4 endpoints)
- Patrón Outbox

**Líneas de Código:** ~2500

**Clases Incluidas:**
```
DOMAIN:
  ├── Value Objects: ContactPoint, Gender, Extension, CostCenter, HeadcountPlan, PartyIdentifierType
  ├── Entities: PartyIdentifier, OrgHierarchy, Job, WorkerProfile, AcademicProfile, StatusLog
  └── Aggregates: Person, OrgUnit, Position, Relationship

APPLICATION:
  ├── Use Cases: CreatePersonUseCase, CreateOrgUnitUseCase, CreatePositionUseCase, CreateRelationshipUseCase
  ├── DTOs: CreatePersonRequest/Response, CreateOrgUnitRequest/Response, etc.

INFRASTRUCTURE:
  ├── JPA: PersonJpa, OrgUnitJpa, PositionJpa, RelationshipJpa
  ├── Repositories: PersonRepository, OrgUnitRepository, PositionRepository, RelationshipRepository
  ├── Outbox: EventOutbox
  └── EventOutboxRepository

API:
  └── Controllers: PersonController, OrgUnitController, PositionController, RelationshipController
```

### Archivo 2: bc1_workforce_org_master_adapters_v1.txt (Complementario)
**Contiene:**
- Puertos (Interfaces de aplicación)
- Mappers completos (Domain → JPA, Domain → DTO, Domain → Event JSON)
- Excepciones personalizadas (7 clases)
- Configuración Spring (application.yml, dependencies pom.xml)
- Checklist de implementación

**Líneas de Código:** ~1500

**Clases Incluidas:**
```
PORTS:
  ├── PersonRepositoryPort, OrgUnitRepositoryPort, PositionRepositoryPort
  ├── RelationshipRepositoryPort, EventOutboxPort

MAPPERS:
  ├── PersonMapper, OrgUnitMapper, PositionMapper, RelationshipMapper

EXCEPTIONS:
  ├── SolverException (base)
  ├── PersonAlreadyExistsException, PersonNotFoundException
  ├── OrgUnitNotFoundException, JobNotFoundException
  ├── HeadcountExceededException, InvalidRelationshipException
```

### Archivo 3: bc1_workforce_org_master_ddl_v1.sql (DDL SQL)
**Contiene:**
- 11 tablas principales
- Índices estratégicos
- Triggers para auditoría
- Vistas para consultas comunes
- Scripts de rollback

**Tablas Creadas:**
```
person, party_identifier, org_unit, org_hierarchy, job, position,
relationship, worker_profile, academic_profile, status_log, event_outbox
```

## Ubicación en Proyecto
```
C:\Users\usuario\IdeaProjects\ProyectoAI\code\
├── bc1_workforce_org_master_v1.txt           (Principal)
├── bc1_workforce_org_master_adapters_v1.txt  (Complementario)
└── bc1_workforce_org_master_ddl_v1.sql       (DDL PostgreSQL)
```

================================================================================
3. ESTRUCTURA DE PAQUETES EN PROYECTO
================================================================================

```
core-platform/
└── src/main/java/com/solveria/core/
    └── workforce/
        ├── domain/
        │   ├── model/
        │   │   ├── Person.java
        │   │   ├── OrgUnit.java
        │   │   ├── Position.java
        │   │   ├── Relationship.java
        │   │   ├── WorkerProfile.java
        │   │   ├── AcademicProfile.java
        │   │   ├── PartyIdentifier.java
        │   │   ├── Job.java
        │   │   └── StatusLog.java
        │   ├── model/vo/
        │   │   ├── ContactPoint.java
        │   │   ├── Gender.java
        │   │   ├── Extension.java
        │   │   ├── CostCenter.java
        │   │   ├── HeadcountPlan.java
        │   │   └── PartyIdentifierType.java
        │   └── exception/
        │       ├── SolverException.java
        │       ├── PersonAlreadyExistsException.java
        │       ├── PersonNotFoundException.java
        │       ├── OrgUnitNotFoundException.java
        │       ├── JobNotFoundException.java
        │       ├── HeadcountExceededException.java
        │       └── InvalidRelationshipException.java
        │
        ├── application/
        │   ├── usecase/
        │   │   ├── CreatePersonUseCase.java
        │   │   ├── CreateOrgUnitUseCase.java
        │   │   ├── CreatePositionUseCase.java
        │   │   └── CreateRelationshipUseCase.java
        │   ├── dto/
        │   │   ├── CreatePersonRequest.java
        │   │   ├── PersonResponse.java
        │   │   ├── CreateOrgUnitRequest.java
        │   │   ├── OrgUnitResponse.java
        │   │   ├── CreatePositionRequest.java
        │   │   ├── PositionResponse.java
        │   │   ├── CreateRelationshipRequest.java
        │   │   └── RelationshipResponse.java
        │   └── port/
        │       ├── PersonRepositoryPort.java
        │       ├── OrgUnitRepositoryPort.java
        │       ├── PositionRepositoryPort.java
        │       ├── RelationshipRepositoryPort.java
        │       └── EventOutboxPort.java
        │
        ├── infrastructure/
        │   ├── jpa/
        │   │   ├── PersonJpa.java
        │   │   ├── OrgUnitJpa.java
        │   │   ├── PositionJpa.java
        │   │   └── RelationshipJpa.java
        │   ├── repository/
        │   │   ├── PersonRepository.java (Spring Data JPA)
        │   │   ├── OrgUnitRepository.java
        │   │   ├── PositionRepository.java
        │   │   ├── RelationshipRepository.java
        │   │   └── EventOutboxRepository.java
        │   ├── adapter/
        │   │   ├── PersonMapper.java
        │   │   ├── OrgUnitMapper.java
        │   │   ├── PositionMapper.java
        │   │   └── RelationshipMapper.java
        │   └── outbox/
        │       └── EventOutbox.java
        │
        └── api/
            └── rest/
                ├── PersonController.java
                ├── OrgUnitController.java
                ├── PositionController.java
                └── RelationshipController.java

src/main/resources/
├── application.yml
└── i18n/
    ├── messages_es.properties
    ├── messages_en.properties
    └── messages_pt.properties

sql/
└── bc1_workforce_org_master_ddl_v1.sql
```

================================================================================
4. GUÍA DE IMPLEMENTACIÓN POR FASES
================================================================================

### FASE 1: Preparación del Proyecto (1-2 horas)

#### 1.1 Actualizar pom.xml
Copiar dependencias del archivo bc1_workforce_org_master_adapters_v1.txt:
- spring-boot-starter-data-jpa
- postgresql (driver)
- lombok
- jackson-databind
- spring-boot-starter-validation

#### 1.2 Crear estructura de paquetes
```bash
mkdir -p core-platform/src/main/java/com/solveria/core/workforce/{domain,application,infrastructure,api}/{model,usecase,jpa,repository,adapter,rest}
```

#### 1.3 Configurar BD
```bash
# Crear base de datos
createdb core_hr_db

# Ejecutar DDL
psql core_hr_db < code/bc1_workforce_org_master_ddl_v1.sql
```

#### 1.4 Copiar application.yml
Crear `core-platform/src/main/resources/application.yml` con configuración del archivo adapters.

---

### FASE 2: Domain Model (2-3 horas)

#### 2.1 Value Objects
Crear archivos en `domain/model/vo/`:
- ContactPoint.java
- Gender.java
- Extension.java
- CostCenter.java
- HeadcountPlan.java
- PartyIdentifierType.java

**Constructor recomendado:** Static factory methods (create())

#### 2.2 Entities y Aggregates
Crear archivos en `domain/model/`:
- Person.java (Aggregate Root)
- PartyIdentifier.java (Entity)
- OrgUnit.java (Aggregate Root)
- OrgHierarchy.java (Entity)
- Job.java (Entity/VO)
- Position.java (Aggregate Root)
- Relationship.java (Aggregate Root)
- WorkerProfile.java (Entity)
- AcademicProfile.java (Entity)
- StatusLog.java (Entity)

**Invariantes implementadas:**
- Person: age >= 18, globalId unique
- OrgUnit: No unidades huérfanas
- Position: HeadcountPlan.occupy() valida slots
- Relationship: No traslape de vínculos primarios

#### 2.3 Excepciones
Crear archivos en `domain/exception/`:
- SolverException.java (base)
- PersonAlreadyExistsException.java
- PersonNotFoundException.java
- OrgUnitNotFoundException.java
- JobNotFoundException.java
- HeadcountExceededException.java
- InvalidRelationshipException.java

---

### FASE 3: Application Layer (2-3 horas)

#### 3.1 Puertos (Interfaces)
Crear archivos en `application/port/`:
- PersonRepositoryPort.java
- OrgUnitRepositoryPort.java
- PositionRepositoryPort.java
- RelationshipRepositoryPort.java
- EventOutboxPort.java

#### 3.2 Use Cases
Crear archivos en `application/usecase/`:
- CreatePersonUseCase.java
  * Validar deduplicación por globalId
  * Lanzar evento PERSON_CREATED
- CreateOrgUnitUseCase.java (2 métodos: root, child)
  * Validar jerarquía sin huérfanas
  * Lanzar evento ORG_UNIT_CREATED
- CreatePositionUseCase.java
  * Validar OrgUnit y Job existen
  * Lanzar evento POSITION_CREATED
- CreateRelationshipUseCase.java
  * Validar no traslape de vínculos primarios
  * Crear WorkerProfile o AcademicProfile según tipo
  * Lanzar evento RELATIONSHIP_CREATED

#### 3.3 DTOs
Crear archivos en `application/dto/`:
- CreatePersonRequest.java / PersonResponse.java
- CreateOrgUnitRequest.java / OrgUnitResponse.java
- CreatePositionRequest.java / PositionResponse.java
- CreateRelationshipRequest.java / RelationshipResponse.java

**Validaciones JSR-303 requeridas:**
```java
@NotBlank, @NotNull, @Email, @Positive, etc.
```

---

### FASE 4: Infrastructure Layer (3-4 horas)

#### 4.1 Entidades JPA
Crear archivos en `infrastructure/jpa/`:
- PersonJpa.java
- OrgUnitJpa.java
- PositionJpa.java
- RelationshipJpa.java
- EventOutbox.java

**Nota:** Las entidades JPA se mapean exactamente a las tablas SQL del DDL.

#### 4.2 Repositorios (Spring Data)
Crear archivos en `infrastructure/repository/`:
- PersonRepository.java (extends JpaRepository<PersonJpa, UUID>)
- OrgUnitRepository.java
- PositionRepository.java
- RelationshipRepository.java
- EventOutboxRepository.java

**Métodos a incluir:**
```java
// PersonRepository
boolean existsByGlobalId(String globalId);
Optional<PersonJpa> findByGlobalId(String globalId);

// RelationshipRepository
boolean existsByPersonIdAndTenantIdAndRelationTypeAndCurrentStatus(...);

// EventOutboxRepository
List<EventOutbox> findByIsPublishedFalseOrderByCreatedAtAsc();
```

#### 4.3 Mappers
Crear archivos en `infrastructure/adapter/`:
- PersonMapper.java
- OrgUnitMapper.java
- PositionMapper.java
- RelationshipMapper.java

**Cada mapper implementa 4 conversiones:**
```java
public X toPersistenceEntity(DomainX domain)      // Domain → JPA
public DomainX toDomain(XJpa jpa)                 // JPA → Domain
public XResponse toResponse(DomainX domain)       // Domain → DTO
public String toEventPayload(DomainX domain)      // Domain → JSON
```

---

### FASE 5: REST Controllers (1-2 horas)

#### 5.1 Controllers
Crear archivos en `api/rest/`:
- PersonController.java
  * POST /api/v1/persons
- OrgUnitController.java
  * POST /api/v1/org-units/root
  * POST /api/v1/org-units (con parentId)
- PositionController.java
  * POST /api/v1/positions
- RelationshipController.java
  * POST /api/v1/relationships

**Patrón de logging obligatorio:**
```java
log.info("event=API_PERSON_CREATE_REQUEST_RECEIVED firstName={} lastName={}", ...);
log.info("event=API_PERSON_CREATE_SUCCESS personId={}", ...);
log.error("event=API_PERSON_CREATE_ERROR errorCode={} message={}", ...);
```

---

### FASE 6: Global Exception Handler (1 hora)

Crear `api/exception/GlobalExceptionHandler.java`:
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(PersonAlreadyExistsException.class)
    public ResponseEntity<?> handlePersonExists(PersonAlreadyExistsException e) { ... }
    
    @ExceptionHandler(SolverException.class)
    public ResponseEntity<?> handleSolverException(SolverException e) { ... }
    
    // Etc para todas las excepciones
}
```

---

### FASE 7: Testing (2-3 horas)

#### 7.1 Unit Tests (Domain)
```java
PersonTest.java
  - testCreatePerson_WithValidData()
  - testCreatePerson_AgeLessThan18_Throws()
  - testAddIdentifier_WhenPersonHasDuplicate_Throws()

OrgUnitTest.java
  - testCreateRoot_WithoutParent()
  - testCreateChild_WithNullParent_Throws()
  - testHierarchyConstraint_OrphanChild_Throws()

PositionTest.java
  - testOccupy_WhenHasVacancy()
  - testOccupy_WhenNoVacancy_Throws()
```

#### 7.2 Integration Tests
```java
CreatePersonUseCaseTest.java
CreateOrgUnitUseCaseTest.java
CreatePositionUseCaseTest.java
CreateRelationshipUseCaseTest.java
```

#### 7.3 REST Integration Tests
```java
PersonControllerIT.java
OrgUnitControllerIT.java
PositionControllerIT.java
RelationshipControllerIT.java
```

================================================================================
5. TESTING & VALIDACIÓN
================================================================================

### Checklist de Pruebas Manuales

#### Endpoints Críticos a Probar:

**1. POST /api/v1/persons**
```bash
curl -X POST http://localhost:8080/api/v1/persons \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Juan",
    "lastName": "Pérez",
    "birthDate": "1990-05-15",
    "gender": "MALE",
    "globalId": "5900000000",
    "email": "juan@solveria.com"
  }'
```

**2. POST /api/v1/org-units/root**
```bash
curl -X POST http://localhost:8080/api/v1/org-units/root \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: 550e8400-e29b-41d4-a716-446655440000" \
  -d '{
    "name": "Solveria Santa Cruz",
    "unitType": "COMMERCIAL",
    "costCode": "ROOT-SCZ",
    "costDescription": "Raíz para Santa Cruz"
  }'
```

**3. POST /api/v1/positions**
```bash
curl -X POST http://localhost:8080/api/v1/positions \
  -H "Content-Type: application/json" \
  -d '{
    "unitId": "550e8400-e29b-41d4-a716-446655440001",
    "jobId": "550e8400-e29b-41d4-a716-446655440002",
    "maxSlots": 5,
    "isBudgeted": true
  }'
```

**4. POST /api/v1/relationships**
```bash
curl -X POST http://localhost:8080/api/v1/relationships \
  -H "Content-Type: application/json" \
  -d '{
    "personId": "550e8400-e29b-41d4-a716-446655440003",
    "tenantId": "550e8400-e29b-41d4-a716-446655440000",
    "relationType": "LABOR",
    "hireDate": "2026-04-24",
    "employeeNo": "EMP-001",
    "department": "Ventas",
    "jobTitle": "Vendedor"
  }'
```

### Validación de Invariantes

Usar casos de prueba para validar bloques:

1. **Identidad Única:**
   ```java
   // Intento crear dos personas con mismo globalId
   → Debe lanzar PersonAlreadyExistsException
   ```

2. **No Unidades Huérfanas:**
   ```java
   // Intento crear unidad hijo sin parent_id
   → Debe lanzar IllegalArgumentException
   ```

3. **Control de Plazas (Headcount):**
   ```java
   // Intento ocupar más slots que max_slots
   → Debe lanzar HeadcountExceededException
   ```

4. **No Traslape de Vínculos Primarios:**
   ```java
   // Intento crear dos RELATIONSHIP tipo LABOR activos para misma persona
   → Debe lanzar InvalidRelationshipException
   ```

================================================================================
6. NOTAS CRÍTICAS SOBRE INVARIANTES
================================================================================

### Invariante 1: IDENTIDAD ÚNICA
**Ubicación:** CreatePersonUseCase.execute()

```java
boolean personExists = personRepository.existsByGlobalId(request.getGlobalId());
if (personExists) {
    log.warn("event=PERSON_DEDUPLICATION_MATCH_FOUND globalId={}", request.getGlobalId());
    throw new PersonAlreadyExistsException("PersonID ya existe: " + request.getGlobalId());
}
```

**Validación:** UQ constraint en DB + lógica en use case

---

### Invariante 2: NO UNIDADES HUÉRFANAS
**Ubicación:** CreateOrgUnitUseCase.executeChild()

```java
OrgUnit parent = orgUnitRepository.findById(parentId)
    .orElseThrow(() -> new OrgUnitNotFoundException("OrgUnit padre no encontrado"));

if (parent == null) {
    throw new IllegalStateException("Invariante violada: No Unidades Huérfanas");
}
```

**Validación:** FK constraint en DB + lógica en use case

---

### Invariante 3: CONTROL DE PLAZAS (HEADCOUNT)
**Ubicación:** Position.occupy()

```java
public void occupy() {
    if (!PositionStatus.VACANT.equals(status)) {
        throw new IllegalStateException("Solo se puede ocupar una posición vacante");
    }
    headcountPlan.occupy();  // ← Valida currentSlots < maxSlots
    this.status = PositionStatus.OCCUPIED;
}

// En HeadcountPlan
public void occupy() {
    if (!hasVacancy()) {
        throw new IllegalStateException("No hay plazas vacantes. " +
            "Control de Plazas (Headcount): Impide asignar personal...");
    }
    currentSlots++;
}
```

**Validación:** CHECK constraint en DB + lógica en domain model

---

### Invariante 4: NO TRASLAPE DE VÍNCULOS PRIMARIOS
**Ubicación:** CreateRelationshipUseCase.execute()

```java
if (Relationship.RelationshipType.LABOR.equals(relType)) {
    boolean hasPrimaryLabor = relationshipRepository
        .existsByPersonIdAndTenantIdAndRelationTypeAndCurrentStatus(
            request.getPersonId(),
            request.getTenantId(),
            Relationship.RelationshipType.LABOR,
            Relationship.RelationshipStatus.ACTIVE
        );
    
    if (hasPrimaryLabor) {
        throw new InvalidRelationshipException(
            "Invariante violada: Un colaborador no puede tener " +
            "dos jefes administrativos simultáneos"
        );
    }
}
```

**Validación:** UNIQUE constraint en DB + lógica en use case

================================================================================
7. PRÓXIMOS PASOS (BC2 y Superiores)
================================================================================

### BC2: Employment Terms & Regulatory Compliance (El "Cerebro Legal")
**Dominios a implementar:**
- Contracts & Legal Terms
- Compliance & Policy Engine
- Workflow, Audit & Legal Evidence

**Dependencias:**
- BC1: Workforce & Org Master ✓

---

### BC3: Employee Dossier & Talent Foundations (La "Memoria Institucional")
**Dominios a implementar:**
- Digital Kardex & Document Compliance
- Assets & Equipment Assignment
- Talent Inventory & Learning Records

**Dependencias:**
- BC1: Workforce & Org Master ✓
- BC2: Employment Terms ✓

---

### BC4: Accruals, Seniority & Time-Off (El "Reloj de Beneficios")
**Dominios a implementar:**
- Leave, Absences & Permissions
- Seniority, Benefits & Accruals

**Dependencias:**
- BC1: Workforce & Org Master ✓

---

### BC5: Financial & Social Compliance (El "Módulo de Salidas")
**Dominios a implementar:**
- Budget Allocation & Funding Control
- Social Security & Regulatory Compliance

**Dependencias:**
- BC1: Workforce & Org Master ✓

---

### BC6: Interaction & Intelligent Experience (La "Capa Externa")
**Dominios a implementar:**
- Employee & Manager Self-Service (ESS/MSS)
- AI Insights & Predictive Analytics

**Dependencias:**
- Todos los BC anteriores

================================================================================
8. REFERENCIAS Y ARCHIVOS
================================================================================

### Archivos Generados (3)
1. **bc1_workforce_org_master_v1.txt** (~2500 líneas)
   - Domain Model, Application, Infrastructure, Controllers

2. **bc1_workforce_org_master_adapters_v1.txt** (~1500 líneas)
   - Puertos, Mappers, Excepciones, Configuración

3. **bc1_workforce_org_master_ddl_v1.sql**
   - 11 tablas, índices, triggers, vistas, rollback

### Especificaciones de Referencia
- Cap 1 - documentacion modulo core para RRHH.md
- Cap 2 - Matriz de variabilidad multi tenant.md
- AGENTS.md (Convenciones de arquitectura y naming)

### Convenciones Obligatorias
- Logging: `log.info("event=EVENT_NAME key1=value1 key2=value2", ...)`
- Error Codes: Enumerados en ErrorCodes.java
- DTOs: Separados de entidades de dominio
- Multi-Tenant: Filtro por tenantId obligatorio

================================================================================
9. TROUBLESHOOTING
================================================================================

### Error: "No qualifying bean of type PersonMapper"
**Solución:** Asegurar que PersonMapper esté anotado con @Component
en paquete escaneable por Spring.

### Error: "Relationship table already exists"
**Solución:** Ejecutar rollback antes de re-crear:
```sql
DROP TABLE IF EXISTS relationship CASCADE;
```

### Error: "UUID format is invalid"
**Solución:** En application.yml, verificar:
```yaml
spring.jackson.serialization.write-dates-as-timestamps: false
```

### Error: "Maximum pool size exceeded"
**Solución:** Aumentar hikari.maximum-pool-size en application.yml

### Test falla por transacción no persistida
**Solución:** Agregar @Transactional a class de test:
```java
@SpringBootTest
@Transactional
public class CreatePersonUseCaseTest { ... }
```

================================================================================
10. FINALIZACIÓN Y AUDITORÍA
================================================================================

### Checklist Pre-Commit
- [ ] Todos los archivos .java compilados sin errores
- [ ] Tests pasan: `mvn test`
- [ ] Integración tests pasan: `mvn verify`
- [ ] Logging sigue patrón "event=..."
- [ ] No hay hardcoded strings en código
- [ ] DTOs NO exponen entidades JPA
- [ ] Invariantes están documentados
- [ ] DDL ejecutado y tablas verificadas
- [ ] README generado

### Versión y Control
- **Versión:** v1.0
- **Fecha:** 2026-04-24
- **Estado:** LISTO PARA IMPLEMENTACIÓN
- **Cambios esperados:** Mínimos en estructura; ajustes en lógica de validaciones

================================================================================
FIN - BC1 WORKFORCE & ORG MASTER - README IMPLEMENTACIÓN v1.0
================================================================================

