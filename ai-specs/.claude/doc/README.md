# `.claude/doc` — no usar como destino canónico de planes

Esta ruta **no** es el lugar oficial para planes de implementación en este repositorio.

## Destino canónico

- **Planes backend:** `ai-specs/changes/[redmine_id]_backend.md`
- **Planes frontend:** `ai-specs/changes/[redmine_id]_frontend.md`

Los comandos `/plan-backend-ticket` y `/plan-frontend-ticket` (definición en `ai-specs/.commands/`) y los agentes en `ai-specs/.agents/` escriben y referencian **solo** esas rutas bajo `ai-specs/changes/`.

## Motivo

Un solo lugar para planes evita deriva entre carpetas y mantiene trazabilidad con el id de Redmine, según `ai-specs/specs/flujo-desarrollo-standards.md` y `docs/guia-sdd.md`.

## Si hay historial local aquí

Si existen archivos antiguos bajo `.claude/doc/`, migrarlos a `ai-specs/changes/` con el nombre `[redmine_id]_backend.md` o `[redmine_id]_frontend.md` y enlazar el ticket en el encabezado del plan.
