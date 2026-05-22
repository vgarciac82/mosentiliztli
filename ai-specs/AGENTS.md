ai-specs/specs/base-standards.mdc

Reglas y especificaciones en español; terminología técnica en inglés.

Este archivo es el punto de entrada comun para Codex cuando se usa dentro de Cursor en este workspace.

Instrucciones operativas:
- Tratar `ai-specs/specs/` como la fuente de verdad del esquema SDD del proyecto.
- Cargar y aplicar siempre `ai-specs/specs/base-standards.mdc` antes de planear, codificar, documentar o revisar cambios.
- Usar `ai-specs/specs/flujo-desarrollo-standards.md` como referencia del flujo SDD completo y sus gates.
- Usar `ai-specs/specs/backend-standards.mdc`, `ai-specs/specs/frontend-standards.mdc` y `ai-specs/specs/documentation-standards.mdc` segun el tipo de tarea.
- Considerar `ai-specs/.commands/` como la definicion canonica de comandos del proyecto.
- Considerar `ai-specs/.agents/` como la definicion canonica de roles de agente del proyecto.
- Tratar `.cursor/commands/` y `.cursor/rules/` solo como adaptadores de Cursor hacia los artefactos canonicos en `ai-specs/`.
- No usar `.cursor/` ni `.vscode/` como fuente de verdad funcional; esas carpetas solo contienen integracion por herramienta.
- Cuando una tarea afecte contratos, arquitectura, reglas o documentacion del proceso, actualizar primero los artefactos relevantes en `ai-specs/specs/` o `ai-specs/changes/` antes del commit.

Regla de interoperabilidad:
- La configuracion especifica del IDE puede variar entre Cursor, VS Code, Antigravity u otras herramientas, pero el comportamiento esperado del agente debe derivarse siempre de `AGENTS.md` y de los artefactos canonicos dentro de `ai-specs/`.