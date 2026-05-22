# Guía de desarrollo

Esta guía define cómo preparar el entorno, desarrollar y validar cambios en proyectos que usan esta base **Spec-Driven Development (SDD)**.

## 1) Contexto del proyecto

Este repositorio es una base SDD donde las especificaciones en `ai-specs/specs/` son la **fuente de verdad**.
El flujo recomendado es: **specs → plan → implementación → documentación**.

## 1.1) Patron neutral-IDE

Este proyecto sigue un patron **neutral-IDE** para que el flujo SDD funcione de forma consistente en Cursor, VS Code, Codex, Claude, Antigravity u otras herramientas compatibles.

### Principio de separacion

- `ai-specs/specs/` contiene la fuente de verdad: estandares, reglas, contratos y flujo SDD.
- `ai-specs/.commands/` contiene la definicion canonica de comandos reutilizables.
- `ai-specs/.agents/` contiene la definicion canonica de roles de agente.
- `.cursor/`, `.vscode/`, `.claude/` u otras carpetas equivalentes contienen solo adaptadores o integraciones por herramienta.
- `AGENTS.md`, `codex.md`, `CLAUDE.md` y archivos equivalentes deben apuntar al mismo nucleo de reglas en `ai-specs/specs/`.

### Regla operativa

Ninguna herramienta debe introducir reglas de negocio, arquitectura o proceso que contradigan `ai-specs/`.
Si existe diferencia entre la configuracion del IDE y los artefactos SDD, **prevalece `ai-specs/`**.

### MCP y configuracion local

- La configuracion MCP del IDE puede vivir en `.cursor/mcp.json`, `.vscode/mcp.json` o archivos equivalentes.
- La configuracion global de Codex puede vivir en `~/.codex/config.toml`.
- Estas configuraciones son de integracion y transporte; no forman parte de la fuente de verdad SDD.
- No guardar secretos reales en artefactos versionados del proyecto. Usar placeholders, variables de entorno o configuracion local fuera del repositorio cuando aplique.

### Resultado esperado

Un desarrollador debe poder abrir el proyecto desde distintos IDEs y obtener el mismo comportamiento funcional del agente porque:
- las reglas viven en `ai-specs/specs/`
- los comandos viven en `ai-specs/.commands/`
- `AGENTS.md` actua como punto de entrada comun para Codex en Cursor y otras superficies compatibles

## 2) Stack tecnológico y estándares oficiales

### Backend (estándar principal)
- **Java 21 (OpenJDK / Eclipse Temurin)**
- **Spring Boot 3.5.x**
- **Spring Framework 6.x**
- **Maven**
- **Spring Data JPA + Hibernate**
- **PostgreSQL 17+**
- **Flyway** para migraciones

### Testing y calidad
- **JUnit 5**, **Mockito**, **Spring Boot Test**
- **JaCoCo** con umbral mínimo de cobertura: **90%**
- Validaciones de calidad en pipeline (tests en verde + code review)

### Frontend (si aplica en el proyecto consumidor)
- **React** (JS/TS)
- Patrón de **service layer** (`src/services/`)
- Uso de variables de entorno (ej.: `REACT_APP_API_URL`)
- Manejo explícito de estados de carga/error

### Despliegue y operación
- **Docker / Docker Compose**
- Arquitectura **gateway-first** (exposición pública solo por gateway)
- Seguridad centralizada (JWT, rate limiting, validación estricta)

## 3) Convenciones de arquitectura

### DDD + layered architecture
- Flujo de capas: **Presentation → Application → Domain ← Infrastructure**
- `Domain` no depende de frameworks
- Repositorios definidos por contrato en dominio e implementados en infraestructura
- Controllers finos; lógica de negocio en servicios/modelos de dominio

### Convenciones de código y diseño
- Mantener SRP, OCP, DIP y DRY
- DTOs para separar contrato API de modelos internos
- Validación de entrada en capa application/presentation
- Manejo uniforme de errores (mapeo dominio → HTTP)

### Convenciones Git
- Trabajar en **feature branches** pequeñas y enfocadas
- Commits descriptivos en español
- PR con revisión técnica antes de merge

## 4) Requisitos previos

Instalar:
- **Java 21**
- **Maven** (o Maven Wrapper si el proyecto lo incluye)
- **Docker** y **Docker Compose**
- **Git**
- **Node.js 24.14.x**

## 5) Configuración de entorno (ejemplo)

> Nunca versionar secretos reales.

### Backend (`backend/.env` o variables del sistema)
```env
DB_HOST=localhost
DB_PORT=5432
DB_NAME=app_db
DB_USERNAME=app_user
DB_PASSWORD=<SECRET>
SPRING_PROFILES_ACTIVE=dev
```

### Frontend (`frontend/.env`)
```env
REACT_APP_API_URL=http://localhost:8080
```

## 6) Base de datos local (Docker)

```bash
docker-compose up -d
docker-compose ps
```

Valores típicos:
- Host: `localhost`
- Port: `5432`
- Engine: PostgreSQL 15+

## 7) Backend: ejecución local (Spring Boot + Maven)

```bash
cd backend

# Build + tests
./mvn clean install

# Run app
./mvn spring-boot:run
```

Comandos útiles:
```bash
./mvn test
./mvn test -Dtest=CandidateServiceTest
./mvn verify
./mvn clean package
./mvn test jacoco:report
```

## 8) Frontend: ejecución local (si aplica)

```bash
cd frontend
npm install
npm start
```

## 9) Testing

### Backend
- Unit tests: `*Test.java`
- Integration tests: `*IT.java`
- Ubicación: `src/test/java` replicando estructura productiva
- Patrones recomendados: AAA / Given-When-Then, `@DisplayName`, `@Nested`

Ejecución:
```bash
cd backend
./mvn test
./mvn verify
./mvn test jacoco:report
```

### Frontend (si aplica)
```bash
cd frontend
npm test
npm run cypress:run
npm run cypress:open
```

## 10) Seguridad y operación (mínimos obligatorios)

- No commitear `.env` ni credenciales
- Validación de entrada estricta
- Gestión segura de secrets (env vars o secret manager)
- Health checks y logs estructurados habilitados
- En producción: imágenes versionadas e inmutables, política de rollback documentada

## 11) Checklist antes de abrir PR

- [ ] Rama de feature creada correctamente
- [ ] Cambios alineados con specs y arquitectura por capas
- [ ] Tests actualizados y en verde
- [ ] Cobertura mínima cumplida (90%)
- [ ] Sin secretos/versionado accidental de credenciales
- [ ] Documentación actualizada (`ai-specs/specs/` y/o `ai-specs/changes/` cuando aplique)
