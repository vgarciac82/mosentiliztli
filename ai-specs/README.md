# ai-specs — Proyecto con estructura base para Spec-Driven Development (SDD)

## Propósito general

Este proyecto tiene como **finalidad** mantener una **configuración base reutilizable** para equipos que desarrollan software con asistencia de IA: reglas, estándares, comandos y convenciones se centralizan aquí para que no dependan de un único editor o de un único proveedor de copilot.

Es **transversal** por diseño: la misma base puede adoptarse en **cualquier IDE o entorno asistido por IA** (por ejemplo Cursor, Visual Studio Code con extensiones de IA, Antigravity, Claude Code, GitHub Copilot, Gemini en el IDE, u otros que sigan convenciones similares de archivos de proyecto). Los puntos de entrada por herramienta (`AGENTS.md`, `CLAUDE.md`, `codex.md`, `GEMINI.md`, carpetas `.cursor/`, `.github/`, `.agent/`, etc.) actúan como **adaptadores** hacia un único núcleo de especificaciones en `ai-specs/specs/`.

En la práctica sirve como **plantilla o repositorio de referencia** que puedes copiar, enlazar o versionar junto a tus aplicaciones para que humanos e IA compartan la misma fuente de verdad sobre cómo se especifica, planifica e implementa el software.

---

Este repositorio es una **estructura base** para aplicar **Spec-Driven Development (SDD)**: desarrollo guiado por especificaciones y estándares. Las especificaciones viven en `ai-specs/specs/` y son la fuente de verdad; los comandos y agentes de IA planifican e implementan siguiendo esas especificaciones.

Se recomienda usarlo junto con frameworks de Spec-Driven Development como [OpenSpec](https://github.com/Fission-AI/OpenSpec).
