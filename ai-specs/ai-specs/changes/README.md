# Planes de implementación (`ai-specs/changes/`)

Aquí viven los **planes de implementación** generados en la Fase 7 del flujo SDD (después de que el ticket en Redmine esté listo para desarrollo).

## Convención de nombres

| Tipo | Patrón |
|------|--------|
| Backend | `[redmine_id]_backend.md` |
| Frontend | `[redmine_id]_frontend.md` |

`[redmine_id]` es el identificador del ticket en Redmine (numérico o clave del proyecto, según lo que use el equipo de forma consistente).

## Estado de ejemplos legacy

Los siguientes archivos se mantienen solo como referencia histórica y **no** deben usarse como plantilla activa:

- `ai-specs/changes/SCRUM-10_backend.md`
- `ai-specs/changes/SCRUM-10-Position-Update.md`

La referencia vigente para planes y estándares está en:

- `ai-specs/changes/[redmine_id]_backend.md`
- `ai-specs/changes/[redmine_id]_frontend.md`
- `ai-specs/specs/backend-standards.mdc`
- `ai-specs/specs/frontend-standards.mdc`
- `ai-specs/specs/documentation-standards.mdc`

## Comandos

- `/plan-backend-ticket` → produce o actualiza el plan backend en esta carpeta.
- `/plan-frontend-ticket` → produce o actualiza el plan frontend en esta carpeta.

Definición canónica de los comandos: `ai-specs/.commands/`. Roles de agente: `ai-specs/.agents/backend-developer.md` y `ai-specs/.agents/frontend-developer.md`.

**No** usar `.claude/doc/` para estos artefactos; ver `.claude/doc/README.md` si existe una nota de deprecación.

## Artefactos SDD que no van aquí

PRD, ADRs, UX Flows y open questions permanecen bajo `document/` en la raíz del repositorio.
