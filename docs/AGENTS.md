# AGENTS.md

## Panorama rapido del repo
- Servicios principales: `ai-service/` (Spring Boot, Clean/Hex) y `iam-service/` (API/orquestacion) con logica de negocio en `core-plataform/` (libreria compartida). Ver `ai-service/README.md` y `iam-service/docs/README.md`.

## Arquitectura y limites clave
- `ai-service` es multi-modulo Maven con dependencias estrictas: `ai-domain` -> `ai-application` -> (`ai-api`, `ai-infrastructure`) -> `ai-bootstrap`. Ver diagrama en `ai-service/README.md`.
- En `ai-domain` y `ai-application` esta prohibido usar Spring (`org.springframework.*`, `@Service`, `@Component`, etc.); el wiring se hace en `ai-bootstrap` (ej. `UseCaseConfig`).
- Spring AI solo se incorpora en `ai-infrastructure` y `ai-bootstrap` (no en domain/app). Ver `ai-service/README.md`.
- `iam-service` contiene solo capa API/orquestacion; la logica vive en `core-plataform`. Flujo: Controller -> Orchestrator -> UseCase (core-platform) -> Domain -> Port -> Adapter -> DB. Ver `iam-service/docs/prompts/000-conventions.md`.

## Convenciones especificas del proyecto
- Naming en `iam-service`: `*Controller` en `api.rest.*`, `*Orchestrator` en `application.orchestration.*`, DTOs `*Request/*Response` en `api.rest.dto.*` y `application.dto.*`. Ver `iam-service/docs/prompts/000-conventions.md`.
- Logging estructurado obligatorio: `event=EVENT_NAME key=value`. Ejemplo: `event=IAM_ROLE_CREATE_SUCCESS roleId=...`. Ver `iam-service/docs/prompts/000-conventions.md`.
- i18n obligatorio en errores: usar `ErrorCodes` y mensajes en `resources/i18n/messages_{locale}.properties`; no hardcodear mensajes. Ver `iam-service/docs/prompts/000-conventions.md`.
- Endpoints REST siguen `/api/v1/{resource}` (plural). Ejemplo: `POST /api/v1/roles`. Ver `iam-service/docs/prompts/000-conventions.md`.

## Flujos de trabajo (dev/build/test)
- `ai-service` build/tests: `mvnw.cmd -q -B clean test` (si falla `clean`, usar `mvnw.cmd -q -B test`). Ver `ai-service/README.md`.
- `ai-service` dev local (PowerShell): `scripts/dev-up.ps1` (Postgres en host `5433`), `scripts/dev-build.ps1`, `scripts/dev-run.ps1` (puerto `8081`). Ver `ai-service/README-DEV.md`.
- Perfil `dev` usa stubs (no requiere OpenAI key); perfil `test` desactiva Redis/Mongo y SpringDoc. Ver `ai-service/README.md` y `ai-service/README-DEV.md`.

## Integraciones y seguridad
- `ai-service` puede usar Postgres (docker), Redis y Mongo (opcionales en `ai-infrastructure`; la app arranca sin ellos). Ver `ai-service/README.md`.
- Seguridad en `ai-service`: solo `/actuator/health` y `/actuator/info` son publicos; Swagger solo en perfil `dev`. Ver `ai-service/README.md`.

