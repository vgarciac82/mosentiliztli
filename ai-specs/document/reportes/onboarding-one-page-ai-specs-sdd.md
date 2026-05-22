# One-Page Onboarding
## `ai-specs` + SDD (Spec-Driven Development)

## Objetivo

Alinear equipo y agentes de IA en un flujo único para convertir requerimientos en software con trazabilidad y calidad desde el primer día.

---

## Qué debes entender primero (60 segundos)

- El PRD es la fuente de verdad.
- Los tickets no sustituyen el PRD: lo refinan.
- No se asumen constraints: se preguntan o se marcan `TBD`.
- Cada fase tiene gates; si un gate falla, no se avanza.
- Se implementa solo cuando la especificación está cerrada y verificable.

---

## Checklist operativo de onboarding (equipo/AI)

### 0) Preparación

- [ ] Leer `ai-specs/specs/base-standards.mdc`.
- [ ] Leer `ai-specs/specs/flujo-desarrollo-standards.md`.
- [ ] Leer `ai-specs/specs/prd-requirements-standards.md`.
- [ ] Confirmar reglas de idioma (técnico en inglés, documentación/tickets en español).

### 1) Discovery Gate (antes del PRD)

- [ ] Hacer mínimo 2 preguntas críticas al stakeholder.
- [ ] Aclarar problema, KPIs, constraints y alcance.
- [ ] Registrar incertidumbres como preguntas o `TBD`.
- [ ] No escribir PRD hasta tener respuestas mínimas.

### 2) PRD (fuente única)

- [ ] Crear/actualizar `document/PRD.md`.
- [ ] Incluir Executive Summary, UX/funcionalidad, especificaciones técnicas y riesgos.
- [ ] Definir User Stories con IDs y Acceptance Criteria verificables.
- [ ] Elaborar análisis crítico en `document/PRD-analisis-critico.md`.

### 3) Gate-PRD (obligatorio)

- [ ] Semántica de negocio sin ambigüedad.
- [ ] Resultado esperado con estructura/tipos definidos.
- [ ] Manejo de errores y casos “no aplica” definido.
- [ ] Idempotencia definida cuando aplique.
- [ ] Estados/transiciones definidos cuando aplique.
- [ ] Escalar dudas a `document/open-questions.md` si hay bloqueos.

### 4) Decidir artefactos (mínimo vs completo)

- [ ] Usar modo mínimo por defecto.
- [ ] Crear UX Flow solo si hay complejidad real de ramas/estados.
- [ ] Crear ADR solo para decisiones arquitectónicas no triviales.
- [ ] Crear US refinada separada solo si AC del PRD no alcanza para pruebas.

### 5) Ticketing Redmine

- [ ] Crear ticket con `/create-us` solo después del Gate-PRD.
- [ ] Incluir trazabilidad al PRD (`US-XX`).
- [ ] Definir AC con al menos un caso de éxito y uno de error.
- [ ] Mantener estado inicial `Design`.

### 6) Enriquecimiento y validación final

- [ ] Ejecutar `/enrich-us` para completar detalle técnico.
- [ ] Pasar Mini Gate-PRD del ticket.
- [ ] Resolver `Diseño Ref` (link o N/A; no dejar ambiguo si hay UI).
- [ ] Ejecutar Gate-final de trazabilidad.
- [ ] Pasar a `Development` solo si todo está en verde.

### 7) Plan y desarrollo

- [ ] Generar plan con `/plan-backend-ticket` y/o `/plan-frontend-ticket`.
- [ ] Implementar con `/develop-backend` y/o `/develop-frontend`.
- [ ] Ejecutar tests, lint y type checking.
- [ ] Crear commit descriptivo y PR enlazado al ticket.

### 8) Cierre de ciclo

- [ ] Verificar trazabilidad completa: PRD -> ticket -> código -> PR.
- [ ] Documentar decisiones arquitectónicas (ADR) si surgieron.
- [ ] Actualizar `open-questions` si quedaron pendientes no bloqueantes.

---

## Reglas de oro (memorizar)

- **Regla 1:** Si no está claro, no se implementa.
- **Regla 2:** Si no es verificable, no está terminado.
- **Regla 3:** Si no traza al PRD, no entra a desarrollo.
- **Regla 4:** Si un artefacto no agrega valor comprobable, no se crea.

---

## Anti-patrones frecuentes

- [ ] Crear tickets sin Gate-PRD.
- [ ] Duplicar contenido del PRD sin refinamiento.
- [ ] Avanzar con `TBD` críticos sin escalar.
- [ ] Implementar sin pruebas ni checks de calidad.
- [ ] Abrir PR sin relación explícita con ticket y requerimiento.

---

## Mapa rápido de rutas del proyecto

- Estándares: `ai-specs/specs/`
- Comandos operativos: `ai-specs/.commands/`
- PRD y artefactos: `document/`
- Planes de implementación: `ai-specs/changes/`

---

## Definición de éxito en onboarding

El onboarding está completo cuando cualquier miembro del equipo (humano o AI) puede ejecutar el flujo completo sin saltarse gates y entregar una funcionalidad con evidencia de calidad y trazabilidad end-to-end.

