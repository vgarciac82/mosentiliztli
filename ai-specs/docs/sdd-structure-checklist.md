# Checklist de auditoría estructural SDD

Checklist operativo para prevenir deriva entre estándares, comandos y artefactos del repositorio.

## Cuándo ejecutar

- Antes de publicar cambios en `ai-specs/specs/`, `ai-specs/.commands/` o `ai-specs/.agents/`.
- Después de agregar o modificar comandos en adaptadores de IDE (`.cursor/commands/`, `.claude/commands/`).
- Antes de crear un release de la plantilla base.

## Validación automática (recomendada)

Ejecutar:

```powershell
powershell -ExecutionPolicy Bypass -File ".\scripts\validate-sdd-structure.ps1"
```

La validación falla si detecta deriva de comandos/adaptadores, rutas canónicas faltantes, uso del alias MCP legacy o posibles secretos en `mcp.json`.

En CI se ejecuta automáticamente con el workflow:

`/.github/workflows/validate-sdd-structure.yml`

## Checklist

### 1) Comandos y adaptadores de IDE

- [ ] Cada comando canónico en `ai-specs/.commands/` tiene entrypoint equivalente en `.cursor/commands/` cuando aplica.
- [ ] Cada comando canónico en `ai-specs/.commands/` tiene entrypoint equivalente en `.claude/commands/` cuando aplica.
- [ ] El flujo SDD mínimo está completo: `create-us`, `enrich-us`, `plan-backend-ticket`, `plan-frontend-ticket`, `develop-backend`, `develop-frontend`.
- [ ] Los adaptadores de IDE apuntan por referencia al comando canónico y no duplican lógica.

### 2) Rutas canónicas y artefactos SDD

- [ ] `document/` mantiene artefactos de requerimientos (`PRD.md`, `PRD-analisis-critico.md`, `open-questions.md`, `ux-flows/`, `adrs/`, `user-stories/`).
- [ ] `ai-specs/changes/` solo contiene planes de implementación (`[redmine_id]_backend.md`, `[redmine_id]_frontend.md`) o archivos etiquetados como legacy.
- [ ] `evidence/` se usa para evidencia de seguridad y auditoría según `security-standards.md`.
- [ ] No existen rutas de destino alternativas para planes (por ejemplo `.claude/doc/`) como ruta activa.

### 3) Coherencia normativa

- [ ] `ai-specs/specs/base-standards.mdc` se mantiene como fuente única de verdad.
- [ ] `flujo-desarrollo-standards.md` y `prd-requirements-standards.md` siguen alineados entre sí.
- [ ] Los ejemplos y plantillas no contradicen el stack normativo (`backend-standards.mdc`, `frontend-standards.mdc`).
- [ ] Si un archivo histórico contradice estándares vigentes, está marcado explícitamente como legacy.

### 4) Idioma y documentación

- [ ] Narrativa de documentación y AI specs en español.
- [ ] Terminología técnica en inglés.
- [ ] Enlaces locales críticos (`README`, `document`, `evidence`, `specs`) resuelven a rutas existentes.
- [ ] Cambios de API reflejados en `ai-specs/specs/api-spec.yml` cuando aplica.
- [ ] El MCP canónico se referencia como `redmine-cnf` (sin alias legacy).
- [ ] `mcp.json` versionados solo contienen placeholders o variables de entorno para `X-API-Key`.

### 5) Resultado de auditoría

- [ ] Se documenta fecha de auditoría y responsable.
- [ ] Se registran hallazgos y acciones correctivas.
- [ ] Si hay bloqueo SDD, no avanzar a implementación hasta resolverlo.

## Registro sugerido

| Fecha | Responsable | Hallazgos | Acción |
|---|---|---|---|
| YYYY-MM-DD | Nombre/rol | Sin hallazgos / Lista breve | N/A / enlace a PR |
