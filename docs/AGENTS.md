# AGENTS.md – Guide for AI Coding Agents

This document provides essential knowledge for AI agents working in the **PeopleCoreIA** multi-repo workspace (Solveria platform).

## 🏗️ Architecture Overview

This is a **multi-repo microservices project** with strict dependency management:

```
core-platform (Library)
    ├── domain/             → DDD entities + business logic (with JPA annotations - see ADR-001)
    ├── application/        → Use cases, ports/interfaces
    └── infrastructure/     → Adapters (JPA, MongoDB, Redis, OpenAI)
        ↓
    Published as JAR to local Maven repo
        ↓ depends on
    ├── iam-service        → Identity/Access Management microservice
    └── ai-service         → LLM completions, RAG, vector search microservice
```

**Critical Rule:** Services depend on core-platform. Build order matters: **core-platform first (mvn install), then services (mvn verify).**

## 🔑 Key Architectural Patterns

### 1. **Multi-Repo Dependency Resolution**
- **core-platform** uses `mvn install` → publishes JAR to **local Maven repository** (`~/.m2/`)
- **iam-service** and **ai-service** use `mvn verify` → resolves core-platform from local repo
- **Always use helper scripts:** `.\scripts\build-all.ps1` handles this automatically
- **❌ DON'T:** `cd iam-service && mvnw verify` (fails if core-platform not installed)
- **✅ DO:** `.\scripts\build-all.ps1` or `.\scripts\install-core.ps1` + service builds

### 2. **Layer Separation (Core Platform)**
```
Domain (com.solveria.core.{domain}.model.*)
  └─ Pure business logic, DDD aggregates
  └─ Contains JPA annotations (pragmatic decision - see core-plataform/adr/ADR-001-Core-Pragmatic-JPA.md)
  └─ NO framework dependencies beyond JPA

Application (com.solveria.core.{domain}.application.*)
  └─ Use cases, commands, ports (interfaces)
  └─ NO framework dependencies (pure Java)
  └─ Port = interface contract (e.g., RoleRepository, LlmChatPort)

Infrastructure (com.solveria.core.{domain}.infrastructure.*)
  └─ Adapter implementations (JPA @Repository, Spring service wrappers)
  └─ Spring beans, external API clients
  └─ Database queries, messaging, cache logic
```

### 3. **Layer Separation (IAM Service)**
```
api.rest.* (REST Controllers, DTOs, Exception Handlers)
  └─ NO business logic (all validated in core-platform use cases)
  └─ DTO validation using JSR-303 (@Valid, @NotNull, etc.)
  └─ Maps REST requests → application DTOs → core commands

application.orchestration.* (Orchestrators)
  └─ Coordinates calls to core-platform use cases
  └─ Transforms DTOs between REST layer and core layer
  └─ NO business logic validation (delegated to core)

config.* (@Configuration classes)
  └─ Spring bean definitions
  └─ i18n MessageSource setup
  └─ OpenAPI/Swagger configuration
```

### 4. **AI Service Clean Architecture**
```
domain/          → Pure business logic (payment calculations, RAG strategy)
application/     → Use cases, ports (LlmPort, VectorStorePort)
infrastructure/  → Adapters (SpringAiLlmAdapter, PgVectorAdapter, MongoDbAdapter)
api/             → Controllers, DTOs
bootstrap/       → Spring Boot main, profiles (dev/test/prod)
```

## 📝 Essential Naming & Coding Conventions

### Package Structure (IAM Service)
```
com.solveria.iamservice
├── api/                    # REST layer
│   ├── rest/              # *Controller classes
│   ├── exception/         # GlobalExceptionHandler, ErrorCodes
│   └── dto/               # *Request, *Response DTOs
├── application/           # Application layer
│   ├── orchestration/     # *Orchestrator classes
│   ├── dto/               # Internal DTOs
│   └── exception/         # Service-specific exceptions
└── config/                # @Configuration classes
```

### Class Naming
- **Controllers:** `*Controller` (e.g., `RoleController`)
- **Orchestrators:** `*Orchestrator` (e.g., `CreateRoleOrchestrator`, `AssignPermissionsToRoleOrchestrator`)
- **DTOs:** `*Request`, `*Response` (e.g., `CreateRoleRequest`, `RoleResponse`)
- **Exception Handler:** `GlobalExceptionHandler`
- **Configurations:** `*Config` (e.g., `OpenApiConfig`, `I18nConfig`)

### REST Endpoints
- **Base:** `/api/v1/{resource}`
- **Resource names:** Plural (e.g., `/api/v1/roles`, `/api/v1/permissions`)
- **Methods:** `POST /api/v1/roles`, `GET /api/v1/roles/{id}`, `PUT /api/v1/roles/{id}`, `DELETE /api/v1/roles/{id}`

## 🔍 Structured Logging & Error Handling

### Logging Format (MANDATORY)
```java
// Pattern: event=EVENT_NAME key1=value1 key2=value2
log.info("event=IAM_ROLE_CREATE_REQUEST_RECEIVED name={} description={}", 
         request.name(), request.description());
log.info("event=IAM_ROLE_CREATE_SUCCESS roleId={} name={}", 
         role.getId(), role.getName());
log.error("event=IAM_ROLE_CREATE_ERROR errorCode={} name={}", 
         e.getCode(), request.name(), e);
log.warn("event=IAM_ROLE_CREATE_VALIDATION_FAILED field={} reason={}", 
         "name", "required");
```

### Event Naming Convention
- **Prefix:** `{SERVICE}_` (e.g., `IAM_` for iam-service)
- **Format:** `{SERVICE}_{OPERATION}_{STATUS}`
- **Status values:** `REQUEST_RECEIVED`, `SUCCESS`, `ERROR`, `VALIDATION_FAILED`

### Error Codes (NEVER Hardcode Messages)
```java
// Location: api/exception/ErrorCodes.java
public static final String IAM_ROLE_CREATE_FAILED = "IAM_ROLE_CREATE_FAILED";
public static final String IAM_ROLE_NOT_FOUND = "IAM_ROLE_NOT_FOUND";
public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
```

**Messages file location:** `resources/i18n/messages_{locale}.properties` (es, en, pt)
```properties
IAM_ROLE_CREATE_FAILED=Error al crear el rol
IAM_ROLE_NOT_FOUND=No se encontró el rol con identificador {id}
validation.role.name.required=El nombre del rol es obligatorio
```

**Exception flow:**
1. Core platform throws `SolverException(errorCode)` with enum/constant
2. Orchestrator/Controller logs and propagates
3. `GlobalExceptionHandler` catches, resolves message via `MessageSource`, returns JSON

## ⚙️ Developer Workflows (PowerShell)

### Build Commands
```powershell
# Build ALL repos in correct order (core → iam → ai)
.\scripts\build-all.ps1

# Build without tests (faster)
.\scripts\build-all.ps1 -SkipTests

# Install core-platform only to local Maven repo
.\scripts\install-core.ps1

# Test all repos
.\scripts\test-all.ps1
```

### AI Service Development (Requires Docker)
```powershell
# Start Postgres (Docker)
cd ai-service
.\scripts\dev-up.ps1        # Starts Postgres on port 5433 (inside: 5432)

# Build
.\scripts\dev-build.ps1

# Run with dev profile (stubs enabled, no OpenAI key needed)
.\scripts\dev-run.ps1       # Starts on port 8081

# Run on custom port
.\scripts\dev-run.ps1 -Port 8082

# Verify health (should show "UP" status)
curl.exe "http://localhost:8081/actuator/health"

# Stop Postgres
.\scripts\dev-down.ps1
```

### IAM Service Development
```powershell
cd iam-service

# Build + Tests
.\mvnw clean verify

# Run locally (port 8080)
.\mvnw spring-boot:run

# Contract tests (Pact)
.\mvnw test -Ppact

# Swagger: http://localhost:8080/swagger-ui.html
```

## 🔧 Profiles & Configuration

### Profiles
- **dev:** Local development, stubs enabled, no API keys needed
- **test:** Integration testing, in-memory H2/embedded MongoDB
- **prod:** Production, real APIs, JWT required

### Environment Variables
**ai-service/.env** (local development):
```bash
AI_PG_HOST_PORT=5433
# OPENAI_API_KEY=sk-...  # Only needed for prod profile
```

**iam-service:** Uses `application-dev.yml` defaults (no .env needed)

## 🧪 Testing Conventions

### Unit Tests
```powershell
mvnw test
```

### Integration Tests
```powershell
mvnw verify  # Runs unit + integration (Maven Failsafe)
```

### Architecture Tests (ArchUnit)
- **Location:** `core-platform/src/test/java/CoreArchitectureTest.java`
- **Purpose:** Validates Clean Architecture boundaries (domain/application/infrastructure)

### Contract Tests (Pact)
- **Location:** `iam-service/src/test/java/*PactTest.java`
- **Purpose:** Provider-side Pact verification for microservice contracts
```powershell
mvnw test -Ppact
```

## 🚨 Critical Gotchas

### 1. **Build Order Dependency**
- ❌ Don't: `cd core-platform && mvnw verify` (uses verify instead of install)
- ✅ Do: Use `.\scripts\build-all.ps1` or `.\scripts\install-core.ps1`
- **Impact:** Services can't find core-platform JAR if core is not installed to local Maven repo

### 2. **JPA in Domain (Technical Debt)**
- Core-platform **pragmatically uses JPA annotations in domain** (see ADR-001)
- **Not true pure DDD** but accepted for enterprise velocity
- Future refactoring planned only if core needs to support non-JPA persistence

### 3. **Layer Violation: Don't Put Business Logic in API Layer**
- ❌ Validation logic in Controller/Orchestrator
- ✅ All business rules in core-platform use cases
- **Exception handling should log but not mutate state**

### 4. **Never Hardcode Error Messages**
- ❌ `throw new Exception("El rol ya existe")`
- ✅ `throw new SolverException("IAM_ROLE_EXISTS")` + i18n lookup

### 5. **DTOs Don't Expose Domain Entities**
- ❌ `return role` (exposes @Entity directly in API)
- ✅ `return roleToResponseMapper.map(role)` (returns DTO with controlled fields)

### 6. **AI Service Stubs for Dev**
- Dev profile uses **stub implementations** (no real OpenAI calls in dev)
- Look for `@Profile("dev")` + `@Bean` or `*Stub.java` classes
- **Error:** "No qualifying bean of type LlmChatPort" = wrong profile or stub not active

## 📚 Key Files to Reference

| Purpose | Location |
|---------|----------|
| Multi-repo build workflow | `scripts/build-all.ps1` |
| Naming & logging rules | `iam-service/docs/prompts/000-conventions.md` |
| IAM Service API setup | `iam-service/docs/prompts/010-bootstrap-iam-service.md` |
| Exception handling | `iam-service/docs/prompts/040-global-exception-handler.md` |
| Contract testing | `iam-service/docs/prompts/060-contract-testing-mockmvc.md` |
| JPA technical debt | `core-plataform/adr/ADR-001-Core-Pragmatic-JPA.md` |
| Architecture tests | `core-platform/src/test/java/CoreArchitectureTest.java` |
| Pact module example | `iam-service/src/test/java` |

## ✅ Definition of Done (DoD) Checklist

Before committing new features:
- [ ] All 3 repos build: `.\scripts\build-all.ps1`
- [ ] Tests pass: `.\scripts\test-all.ps1`
- [ ] Logs use `event=...` format
- [ ] Error codes in `ErrorCodes.java`, messages in i18n files
- [ ] No hardcoded strings or messages in code
- [ ] DTOs properly separate API/application layers from domain
- [ ] Services run locally: `.\scripts\dev-run.ps1` (ai) or `mvn spring-boot:run` (iam)
- [ ] Health checks work: `curl http://localhost:8080/actuator/health`
- [ ] Swagger UI accessible and documented

---

**Last Updated:** 2026-04-24  
**Stack:** Spring Boot 3.4.0, Java 21, Maven, Docker, PostgreSQL, MongoDB, Redis  
**Related:** `README.md`, `README-DEV.md`, `iam-service/docs/README.md`

