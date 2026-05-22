# Guía SDD — Spec-Driven Development con IA

**Referencia rápida** para el equipo: resume el proceso SDD de este repositorio. **No** sustituye la norma completa ni las reglas cargadas por el IDE (por ejemplo `.cursor/rules/`, `base-standards.mdc`); no debe copiarse como regla duplicada en el editor.

- **Proceso normativo detallado:** [flujo-desarrollo-standards.md](../ai-specs/specs/flujo-desarrollo-standards.md)
- **Gates PRD, requisitos y Gate-final (tablas completas):** [prd-requirements-standards.md](../ai-specs/specs/prd-requirements-standards.md)

**Ciclo de vida completo del desarrollo de software asistido por IA.**
Cada fase produce artefactos verificables. Cada Gate bloquea el avance si el estado no es ✅.

> **Principio rector:** cada artefacto derivado añade información comprobable nueva respecto al PRD. No se reescribe ni duplica contenido; se refina.

---

## Artefactos del ciclo completo

| Artefacto | Ruta | Fase |
|---|---|---|
| PRD | `document/PRD.md` | Fase 1 |
| Análisis crítico | `document/PRD-analisis-critico.md` | Fase 1 |
| Open Questions | `document/open-questions.md` | Continuo |
| UX Flows | `document/ux-flows/UX-<id>-<slug>.md` | Fase 3 (si aplica) |
| ADRs | `document/adrs/ADR-<nnn>-<slug>.md` | Fase 3 (si aplica) |
| US refinadas | `document/user-stories/US-<id>-<slug>.md` | Fase 3 (si aplica) |
| Ticket Redmine | — | Fase 4 |
| Planes de implementación | `ai-specs/changes/[id]_backend.md` / `_frontend.md` | Fase 7 |
| Pull Request | — | Fase 8 |

---

## Comandos disponibles

| Comando / Herramienta | Propósito |
|---|---|
| MCP `redmine-cnf` | Crear y actualizar tickets en Redmine |
| `/create-us` | Crear ticket con estructura obligatoria |
| `/enrich-us` | Enriquecer ticket con detalle técnico + Mini Gate-PRD |
| `/plan-backend-ticket` | Generar plan de implementación backend |
| `/plan-frontend-ticket` | Generar plan de implementación frontend |
| `/develop-backend` | Guiar la implementación del backend |
| `/develop-frontend` | Guiar la implementación del frontend |

---

## Paso a paso

### Fase 0 — Discovery Gate

**Objetivo:** obtener información suficiente del stakeholder antes de escribir cualquier documento.

Formula **≥ 2 de estas preguntas** y espera respuesta real antes de continuar:

1. ¿Qué problema resuelve esto y qué pasa si NO lo hacemos?
2. ¿Cómo medimos el éxito? ¿Contra qué baseline?
3. ¿Qué restricciones existen: tiempo, stack, integraciones, presupuesto?
4. ¿Qué queda fuera del alcance explícitamente?

> Si el stakeholder no define algo → marcarlo como `TBD` con justificación. **Nunca asumir constraints no declarados.**

✅ Avanza cuando tengas respuestas reales documentadas.
❌ No escribas el PRD si todavía no has preguntado.

---

### Fase 1 — PRD (Fuente de verdad)

**Artefacto:** `document/PRD.md`

Escribe el PRD con estas cuatro secciones:

| Sección | Contenido |
|---|---|
| Executive Summary | Problem Statement, Proposed Solution, 3–5 KPIs medibles |
| User Experience & Functionality | Personas, User Stories (`US-XX`), AC, Non-Goals |
| Technical Specifications | Arquitectura, estructuras de datos, integraciones, seguridad, manejo de errores |
| Risks & Roadmap | Fases, riesgos técnicos, Open Questions para stakeholders |

Genera también `document/PRD-analisis-critico.md` para detectar inconsistencias y ambigüedades. Toda pregunta abierta al negocio debe tener respuesta antes del Gate-PRD.

**Idioma (precedencia):** en el PRD, las user stories y los AC van en **inglés** (artefacto de especificación). En **Redmine**, la historia y los AC van en **español** (gestión). El traspaso PRD → ticket es traducción y refinamiento, no copia literal.

✅ Avanza cuando el PRD no tenga `TBD` sin justificación explícita.

---

### Fase 2 — Gate-PRD

**Objetivo:** validar que el PRD es implementable antes de derivar cualquier artefacto.

Ejecuta este checklist completo. Todos los ítems deben ser ✅:

```
[ ] ¿Las reglas de negocio tienen semántica unívoca?
[ ] ¿El resultado esperado tiene estructura y tipos definidos?
[ ] ¿Está definido el comportamiento cuando "ninguna regla aplica"?
[ ] ¿Qué falla, cómo se reporta y si el sistema continúa o aborta?
[ ] ¿Re-ejecución: reemplazo vs. acumulación vs. bloqueo?
[ ] ¿Todas las transiciones del recurso de dominio están definidas?
```

❌ Si algún ítem falla → registrar en `document/open-questions.md` con impacto y owner, corregir el PRD y volver a ejecutar el Gate. No crear tickets hasta superar este punto.

---

### Fase 3 — Tabla de decisión de artefactos (modo mínimo por defecto)

**Objetivo:** determinar qué artefactos intermedios generar antes de crear los tickets.

El **modo mínimo es el default**. Solo genera filas adicionales si el disparador aplica. Detalle y matices: [flujo-desarrollo-standards.md](../ai-specs/specs/flujo-desarrollo-standards.md) (Fase 3).

| Condición | Artefacto adicional (además del PRD) | Paquete resultante |
|---|---|---|
| 1–3 US sin integraciones nuevas ni estados complejos | Ninguno — ir directo a `/create-us` | PRD + ticket |
| US con >2 ramas de error o estados no explícitos en el AC | UX Flow en `document/ux-flows/` | PRD + UX Flow + ticket |
| Integración externa nueva o cambio de contrato API/esquema | ADR de integración en `document/adrs/` | PRD + ADR + ticket |
| Decisión arquitectónica no trivial (persistencia, idempotencia, semántica) | ADR | PRD + ADR + ticket |
| AC insuficiente para mapear a pruebas sin reinterpretar el negocio | US refinada en `document/user-stories/` | PRD + US refinada + ticket |
| >5 US o módulo nuevo con múltiples estados y reglas | Modo completo: UX Flows + US refinadas + ADRs | PRD + UX Flows + US refinadas + ADRs + tickets |

> Regla: si un artefacto no añade información observable que no esté ya en el PRD, no lo generes. **Si dudas, quédate en el modo mínimo.**

**Templates disponibles:**
- ADR → `docs/templates/ADR-template.md`
- UX Flow → `docs/templates/UX-Flow-template.md`

---

### Fase 4 — Crear ticket en Redmine (`/create-us`)

**Objetivo:** registrar la historia de usuario en Redmine con estructura completa y trazabilidad al PRD.

El comando puede operar en modo **SDD** (input con `US-XX` del PRD) o **libre** (sin ID de PRD); pasos y validaciones: [flujo-desarrollo-standards.md](../ai-specs/specs/flujo-desarrollo-standards.md) (Fase 4).

Ejecuta `/create-us` con el MCP. El ticket debe tener esta estructura obligatoria:

```
### Clasificación
- Tipo: [Feature | Task] *(en proyecto `sai-nomina`: Feature = historia de usuario; Task = trabajo técnico derivado)*
- Prioridad sugerida: [Baja | Media | Alta | Crítica]
- Justificación: [1-2 frases]
- PRD Ref: US-XX
- Diseño Ref: [link | TBD justificado | N/A — no requiere UI]

### Historia de Usuario
Como [rol], quiero [acción] para que [valor de negocio medible].

### Criterios de Aceptación (Gherkin)
- Dado que [...], cuando [...], entonces [...].
  (≥1 caso de éxito + ≥1 caso de error/validación)

### Notas Técnicas y Riesgos
```

**Estado inicial en Redmine:** `Design`

✅ Avanza cuando `Diseño Ref` esté definido y haya trazabilidad a `US-XX`.
❌ No crees el ticket si no pasó el Gate-PRD o si `Diseño Ref` está en blanco en una US con UI.

---

### Fase 5 — Enriquecer ticket (`/enrich-us`)

**Objetivo:** añadir el detalle técnico necesario para implementar sin reinterpretar el negocio.

Ejecuta `/enrich-us`. El comando añade dos capas al ticket y ejecuta el Mini Gate-PRD interno:

**Capa 1 — Requerimientos** *(solo si hay defectos; no duplicar el PRD)*
- Historia de Usuario refinada (solo el delta respecto al PRD)
- AC Gherkin completos
- TBDs con impacto declarado

**Capa 2 — Detalle técnico** *(siempre)*
- Endpoints: método, URL, request body, response
- Archivos a modificar
- Tests requeridos: unit e integración
- Requisitos no funcionales: seguridad, rendimiento
- Diseño de referencia + estados cubiertos: éxito, vacío, error, carga
- Candidatos a ADR (si se identifica uno, generar antes de implementar)

**Mini Gate-PRD — checklist interno:**

```
[ ] ¿Los AC son observables y no vagos?
[ ] ¿Los casos de error/validación están definidos?
[ ] ¿No hay ambigüedades de negocio sin resolver?
[ ] ¿Idempotencia definida (si aplica)?
[ ] ¿Diseño Ref tiene link confirmado (o N/A justificado)?
```

Si hay ítems ❌ o TBDs bloqueantes → mantener en `Design` + comentario en el ticket + escalar a `document/open-questions.md` con impacto y owner.

---

### Fase 6 — Gate-final

**Objetivo:** verificar trazabilidad e integridad del paquete completo antes de pasar a Development.

**Estándar:** checklist y tablas completas en [prd-requirements-standards.md](../ai-specs/specs/prd-requirements-standards.md) (vía [flujo-desarrollo-standards.md](../ai-specs/specs/flujo-desarrollo-standards.md), Fase 6).

```
[ ] PRD ↔ UX Flows ↔ ticket: identificadores consistentes, sin contradicciones
[ ] Criterios de éxito medibles asociados a la historia
[ ] Cada AC puede mapearse a una prueba sin reinterpretar el negocio
[ ] Artefactos adicionales añaden información nueva vs. el PRD (no son duplicación)
```

**Activadores de dominio (este proyecto)** — aplicar cuando el ticket los toque; si aplica y falta especificación, no pasar a `Development`:

- **Estados y transiciones** (periodos, procesos): transiciones definidas (reaperturas, cancelaciones); auditoría o motivo si aplica.
- **Motor de reglas / DSL:** agregación de resultados múltiples; comportamiento si ninguna condición es verdadera; semántica unívoca de funciones (orden, mayúsculas/minúsculas).
- **Integraciones externas (API ERP):** obtención de datos (por empleado, lote, caché, reintentos); validación del contrato; campos ausentes o null en respuestas.

✅ Si todo es ✅ → cambiar estado del ticket a **`Development`**.
❌ Si hay ❌ → mantener en `Design`, añadir comentario con lista de ítems bloqueantes.

---

### Fase 7 — Plan de implementación

**Objetivo:** generar el plan técnico detallado antes de escribir una sola línea de código.

**Backend:** ejecuta `/plan-backend-ticket`
→ Output: `ai-specs/changes/[redmine_id]_backend.md`
- Contexto DDD: Domain / Application / Presentation
- Step 0: crear branch `feature/[ticket-id]-backend`
- Steps de implementación: validación, servicio, controller, ruta
- Testing checklist y formato de error responses
- Step final: actualizar documentación técnica

**Frontend:** ejecuta `/plan-frontend-ticket`
→ Output: `ai-specs/changes/[redmine_id]_frontend.md`
- Contexto component-based con service layer
- Step 0: crear branch `feature/[ticket-id]-frontend`
- Steps: servicios, componentes (atoms → molecules → organisms → page)
- Consideraciones UI/UX y accesibilidad
- Step final: actualizar documentación técnica

✅ Avanza cuando el plan esté generado y revisado.

---

### Fase 8 — Implementación

**Objetivo:** implementar, probar y entregar el código con trazabilidad completa al ticket.

**Backend (`/develop-backend`):**
1. Leer y entender el ticket y el plan de implementación
2. Identificar archivos relevantes en el codebase
3. Crear branch: `feature/[ticket-id]-backend`
4. Implementar, escribir tests, verificar lint y type checking
5. Commit descriptivo en español con referencia al ticket (`#[ID]`)
6. Push + Pull Request enlazado al ticket Redmine

**Frontend (`/develop-frontend`):**
1. Analizar el diseño desde la URL de referencia
2. Implementar componentes React siguiendo el plan (atoms → page)
3. Aplicar estilos, accesibilidad y elementos reutilizables
4. Commit descriptivo + PR enlazado

> Si durante la implementación aparecen ambigüedades nuevas → actualizar `document/PRD.md` en la sección afectada y registrar en `document/open-questions.md`.

✅ Avanza cuando Gate 4 sea 100% ✅.

---

## Gates — Checklists de control

### Gate 0 — Antes del PRD
```
[ ] Se hicieron ≥2 preguntas críticas al stakeholder
[ ] No se asumieron constraints no declarados
[ ] Las respuestas son suficientes para iniciar el PRD
```

### Gate 1 — Gate-PRD (antes de derivar artefactos)
```
[ ] Semántica de reglas de negocio unívoca
[ ] Estructura y tipo del resultado esperado definidos
[ ] Comportamiento ante "no aplica" definido
[ ] Manejo de errores definido a nivel de requerimiento
[ ] Idempotencia definida (si aplica)
[ ] Estados y transiciones del recurso de dominio definidos
```

### Gate 2 — Antes de crear el ticket
```
[ ] Gate-PRD superado
[ ] Tabla de decisión ejecutada; artefactos intermedios generados (si aplica)
[ ] Valor de negocio, actor y comportamiento esperado son claros
[ ] Trazabilidad a US-XX del PRD confirmada
[ ] Diseño Ref definido: link, TBD justificado o N/A
```

### Gate 3 — Antes de pasar a Development
```
[ ] Todos los AC son verificables (no vagos)
[ ] Comportamiento ante errores/validaciones definido
[ ] Idempotencia definida (si aplica al ticket)
[ ] Sin ambigüedades de negocio sin resolver
[ ] Si hay UI: Diseño Ref tiene link confirmado (no TBD)
[ ] Gate-final superado (trazabilidad PRD ↔ ticket verificada)
[ ] ADRs generados para decisiones arquitectónicas identificadas
```

### Gate 4 — Antes de hacer PR
```
[ ] Tests escritos y pasando
[ ] Lint y type checking sin errores
[ ] Documentación técnica actualizada (ai-specs/changes/, ai-specs/specs/)
[ ] Commit descriptivo en español con referencia al ticket
[ ] Ambigüedades nuevas: PRD actualizado y Open Questions registradas
```

---

## Anti-patrones — Lo que NO debes hacer

| Anti-patrón | Consecuencia |
|---|---|
| Crear tickets sin haber pasado el Gate-PRD | El ticket hereda ambigüedades y se bloquea en `Design` indefinidamente |
| Asumir constraints no declarados por el stakeholder | El sistema se comporta correcto según el supuesto, incorrecto según el negocio |
| Reescribir en el ticket lo que ya dice el PRD sin añadir detalle | Duplicación; viola el principio de refinamiento SDD |
| Generar UX Flows o ADRs sin trigger justificado en la tabla de decisión | Artefactos que no añaden información comprobable nueva; tiempo desperdiciado |
| Tratar TBDs como bloqueos de ticket sin escalar a `open-questions.md` con owner | Las preguntas sin dueño no se resuelven; el bloqueo se vuelve permanente |
| Pasar a `Development` con `Diseño Ref: TBD` en US con UI | El desarrollador implementa sin referencia confirmada; pantallas que hay que rehacer |
| Implementar sin branch dedicado y PR enlazado al ticket | La trazabilidad código ↔ ticket ↔ PRD se rompe; sin audit trail |

---

## Ticket listo para Development — Ejemplo

```
Título:      [US-12] Consulta de periodos de nómina activos
Estado:      Development
Prioridad:   Media
PRD Ref:     US-12
Diseño Ref:  https://figma.com/file/xxxxx

### Historia de Usuario
Como administrador de nómina, quiero consultar los periodos activos
para que pueda iniciar el procesamiento del periodo correcto.

### Criterios de Aceptación (Gherkin)
- Dado que soy administrador autenticado,
  cuando consulto GET /api/periodos?estado=ACTIVO,
  entonces recibo la lista con id, nombre, fechaInicio, fechaFin, estado.
- Dado que no existen periodos activos,
  cuando consulto GET /api/periodos?estado=ACTIVO,
  entonces recibo [] con status HTTP 200.

### Detalle Técnico
- Endpoint: GET /api/periodos?estado=ACTIVO
- Response: { id, nombre, fechaInicio, fechaFin, estado }
- Archivos: PeriodoController.java, PeriodoService.java
- Tests:    PeriodoServiceTest (unit), PeriodoControllerTest (integration)
- Seguridad: requiere rol ADMIN o NOMINA_ADMIN
```

---

## Cheat Sheet — Referencia rápida

| Fase | Acción | Artefacto | Gate de salida |
|---|---|---|---|
| 0 — Discovery | Preguntar al stakeholder (≥2 preguntas) | Respuestas documentadas | Gate 0: sin supuestos |
| 1 — PRD | Redactar PRD + análisis crítico | `document/PRD.md` + `PRD-analisis-critico.md` | — |
| 2 — Gate-PRD | Validar 6 ítems del checklist | `document/open-questions.md` (si hay fallos) | Gate 1: 6/6 ✅ |
| 3 — Decisión | Determinar artefactos intermedios | UX Flows / ADRs (si aplica) | Gate 2: artefactos listos |
| 4 — Crear ticket | Ejecutar `/create-us` vía MCP | Ticket en estado `Design` | Gate 2: trazabilidad + Diseño Ref |
| 5 — Enriquecer | Ejecutar `/enrich-us` + Mini Gate-PRD | Ticket enriquecido (Capa 1 + Capa 2) | Gate 3: todos ✅ |
| 6 — Gate-final | Verificar trazabilidad PRD ↔ ticket | Ticket en estado `Development` | Gate 3: Gate-final ✅ |
| 7 — Planificar | `/plan-backend-ticket` + `/plan-frontend-ticket` | `ai-specs/changes/[id]_backend.md` / `_frontend.md` | — |
| 8 — Implementar | `/develop-backend` + `/develop-frontend` | Branch + commits + PR enlazado | Gate 4: tests ✅, lint ✅, PR ✅ |

---

## Referencias (profundizar)

| Recurso | Ruta |
|---|---|
| Flujo de desarrollo (norma operativa) | [ai-specs/specs/flujo-desarrollo-standards.md](../ai-specs/specs/flujo-desarrollo-standards.md) |
| PRD y gates de requisitos | [ai-specs/specs/prd-requirements-standards.md](../ai-specs/specs/prd-requirements-standards.md) |
| Checklist de auditoría estructural | [docs/sdd-structure-checklist.md](./sdd-structure-checklist.md) |
| Estándares base de código y testing | [base-standards.mdc](../ai-specs/specs/base-standards.mdc) |
| Backend | [backend-standards.mdc](../ai-specs/specs/backend-standards.mdc) |
| Frontend | [frontend-standards.mdc](../ai-specs/specs/frontend-standards.mdc) |
| Documentación | [documentation-standards.mdc](../ai-specs/specs/documentation-standards.mdc) |
| Comandos del flujo (prompts) | `ai-specs/.commands/` (`create-us.md`, `enrich-us.md`, `plan-*`, `develop-*`) |
