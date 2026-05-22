# Artefactos SDD (`document/`)

Esta carpeta es la **raíz de artefactos de especificación** del flujo SDD definido en:

- `ai-specs/specs/flujo-desarrollo-standards.md` — convenciones de rutas y fases.
- `docs/guia-sdd.md` — guía rápida del equipo.

**Idioma:** narrativa en **español**; terminología técnica en **inglés**, según `ai-specs/specs/base-standards.mdc`.

## Contenido esperado

| Artefacto | Ruta |
|-----------|------|
| PRD | `document/PRD.md` |
| Análisis crítico del PRD | `document/PRD-analisis-critico.md` |
| Open Questions | `document/open-questions.md` |
| UX Flows | `document/ux-flows/UX-<id>-<slug>.md` |
| ADRs | `document/adrs/ADR-<nnn>-<slug>.md` |
| US refinadas | `document/user-stories/US-<id>-<slug>.md` |

Los ficheros `PRD.md`, `PRD-analisis-critico.md` y `open-questions.md` en este directorio son **plantillas placeholder** hasta que el equipo los sustituya por contenido real de cada iniciativa.

**Planes de implementación** (post ticket Redmine) **no** viven aquí: van en `ai-specs/changes/[redmine_id]_backend.md` y `ai-specs/changes/[redmine_id]_frontend.md`.

## Plantillas reutilizables

- ADR: `docs/templates/ADR-template.md`
- UX Flow: `docs/templates/UX-Flow-template.md`
